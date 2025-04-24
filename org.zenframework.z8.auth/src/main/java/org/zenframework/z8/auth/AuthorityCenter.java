package org.zenframework.z8.auth;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.util.Collection;

import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.crypto.Digest;
import org.zenframework.z8.server.engine.HubServer;
import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.engine.IAuthorityCenter;
import org.zenframework.z8.server.engine.IInterconnectionCenter;
import org.zenframework.z8.server.engine.IServerInfo;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.engine.ServerInfo;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.exceptions.ServerUnavailableException;
import org.zenframework.z8.server.exceptions.UserNotFoundException;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.RequestDispatcher;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.LoginParameters;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.guid;

public class AuthorityCenter extends HubServer implements IAuthorityCenter {
	private static final String serversCache = "authority.center.cache";

	static public String id = guid.create().toString();

	static private AuthorityCenter instance = null;

	private final boolean clientHashPassword;
	private final boolean cacheEnabled;

	private SessionManager sessionManager;

	public static IAuthorityCenter launch() throws RemoteException {
		if(instance == null) {
			instance = new AuthorityCenter();
			instance.start();
		}
		return instance;
	}

	public AuthorityCenter() throws RemoteException {
		super(ServerConfig.authorityCenterPort());
		clientHashPassword = ServerConfig.webClientHashPassword();
		cacheEnabled = ServerConfig.authorityCenterCache();
	}

	@Override
	public String id() throws RemoteException {
		return id;
	}

	@Override
	public void start() throws RemoteException {
		super.start();

		sessionManager = new SessionManager();
		sessionManager.start();
		
		enableTimeoutChecking(1 * datespan.TicksPerMinute);

		Trace.logEvent("Authority Center JVM startup options: " + ManagementFactory.getRuntimeMXBean().getInputArguments().toString() + "\n\t" + RequestDispatcher.getMemoryUsage());
	}

	@Override
	public void register(IApplicationServer server) throws RemoteException {
		addServer(new ServerInfo(server, server.id()));
		registerInterconnection(server);
	}

	@Override
	public void unregister(IApplicationServer server) throws RemoteException {
		removeServer(server);
		unregisterInterconnection(server);
	}

	private void registerInterconnection(IApplicationServer server) throws RemoteException {
		try {
			ServerConfig.interconnectionCenter().register(server);
		} catch(Throwable e) {
		}
	}

	private void unregisterInterconnection(IApplicationServer server) throws RemoteException {
		try {
			ServerConfig.interconnectionCenter().unregister(server);
		} catch(Throwable e) {
		}
	}

	@Override
	public IUser register(LoginParameters loginParameters, String password, String requestHost) throws RemoteException {
		if(password == null || password.isEmpty())
			throw new AccessDeniedException();
		
		return getServerInfo().getServer().registerUser(loginParameters, password, requestHost);
	}
	
	@Override
	public IUser verify(String verification, String schema, String requestHost) throws RemoteException {
		return getServerInfo().getServer().verifyUser(verification, schema, requestHost);
	}
	
	@Override
	public IUser remindInit(String login, String schema, String requestHost) throws RemoteException {
		return getServerInfo().getServer().remindInit(login, schema, requestHost);
	}
	
	@Override
	public IUser remind(String verification, String schema, String requestHost) throws RemoteException {
		return getServerInfo().getServer().remind(verification, schema, requestHost);
	}

	@Override
	public IUser changePassword(String verification, String password, String schema, String requestHost) throws RemoteException {
		return getServerInfo().getServer().changeUserPassword(verification, password, schema, requestHost);
	}

	@Override
	public ISession authorize(String sessionId, String serverId) throws RemoteException {
		return sessionManager.getSession(sessionId).setServerInfo(nextServer(serverId));
	}

	@Override
	public ISession login(LoginParameters loginParameters, String password) throws RemoteException {
		IServerInfo serverInfo = getServerInfo();
		IApplicationServer loginServer = serverInfo.getServer();

		if(password == null)
			password = "";

		IUser user;

		try {
			user = loginServer.user(loginParameters, password);
		} catch (UserNotFoundException ignored) {
			throw new AccessDeniedException();
		}

		return sessionManager.create(user).setServerInfo(serverInfo);
	}

	@Override
	public ISession trustedLogin(LoginParameters loginParameters, boolean createIfNotExist) throws RemoteException {
		IServerInfo serverInfo = getServerInfo();
		try {
			return login0(serverInfo, loginParameters);
		} catch (UserNotFoundException e) {
			if (!createIfNotExist)
				throw e;
			serverInfo.getServer().create(loginParameters);
			return login0(serverInfo, loginParameters);
		}
	}

	private ISession login0(IServerInfo serverInfo, LoginParameters loginParameters) throws RemoteException {
		IUser user = serverInfo.getServer().user(loginParameters, null);
		return sessionManager.create(user).setServerInfo(serverInfo);
	}

	private IServerInfo getServerInfo() throws RemoteException {
		IServerInfo serverInfo = nextServer();

		if(serverInfo == null)
			throw new AccessDeniedException();
		return serverInfo;
	}

	@Override
	public void userChanged(guid user, String schema) {
		sessionManager.dropUserSessions(user, schema);
	}

	@Override
	public void roleChanged(guid role, String schema) {
		sessionManager.dropRoleSessions(role, schema);
	}

	private IServerInfo nextServer() throws RemoteException {
		return nextServer(null);
	}

	private IServerInfo nextServer(String serverId) throws RemoteException {
		IServerInfo[] servers = getServers();

		for(IServerInfo server : servers) {
			if(serverId != null && !server.getId().equals(serverId))
				continue;

			if(!server.isAlive()) {
				if(server.isDead())
					unregister(server.getServer());
				continue;
			}

			if(serverId == null)
				sendToBottom(server);

			return server;
		}

		throw new ServerUnavailableException();
	}

	@Override
	protected File cacheFile() {
		return cacheEnabled ? new File(Folders.Base, serversCache) : null;
	}

	@Override
	protected void timeoutCheck() {
		instance.checkSessions();
		instance.checkConnections();
	}

	private void checkSessions() {
		sessionManager.check();
	}

	private void checkConnections() {
		try {
			IInterconnectionCenter center = ServerConfig.interconnectionCenter();

			for(IServerInfo server : getServers()) {
				if(server.isAlive())
					center.register(server.getServer());
			}
		} catch(Throwable e) {
		}
	}
}
