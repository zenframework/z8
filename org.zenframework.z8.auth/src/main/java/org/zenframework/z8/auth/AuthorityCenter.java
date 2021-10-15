package org.zenframework.z8.auth;

import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.crypto.Digest;
import org.zenframework.z8.server.engine.*;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.exceptions.UserNotFoundException;
import org.zenframework.z8.server.ldap.LdapAPI;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.RequestDispatcher;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.LoginParameters;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.utils.StringUtils;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.util.Collection;

public class AuthorityCenter extends HubServer implements IAuthorityCenter {
	private static final String serversCache = "authority.center.cache";

	static public String id = guid.create().toString();

	static private AuthorityCenter instance = null;

	private final boolean clientHashPassword;
	private final String ldapUrl;
	private final boolean checkLdapLogin;
	private final Collection<String> ldapUsersIgnore;
	private final boolean ldapUsersCreateOnSuccessfulLogin;
	private final boolean cacheEnabled;

	private SessionManager sessionManager;

	public static IAuthorityCenter launch(ServerConfig config) throws RemoteException {
		if(instance == null) {
			instance = new AuthorityCenter();
			instance.start();
		}
		return instance;
	}

	private AuthorityCenter() throws RemoteException {
		super(ServerConfig.authorityCenterPort());
		clientHashPassword = ServerConfig.webClientHashPassword();
		checkLdapLogin = ServerConfig.checkLdapLogin();
		ldapUrl = ServerConfig.ldapUrl();
		ldapUsersIgnore = ServerConfig.ldapUsersIgnore();
		ldapUsersCreateOnSuccessfulLogin = ServerConfig.ldapUsersCreateOnSuccessfulLogin();
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

		if (!ldapUrl.isEmpty())
			Trace.logEvent("Authority Center uses LDAP: " + ldapUrl);
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
	public IUser register(LoginParameters loginParameters, String password) throws RemoteException {
		if(password == null || password.isEmpty())
			throw new AccessDeniedException();
		
		return getServerInfo().getServer().registerUser(loginParameters, password);
	}
	
	@Override
	public IUser verify(String verification, String schema) throws RemoteException {
		return getServerInfo().getServer().verifyUser(verification, schema);
	}
	
	@Override
	public IUser remindInit(String login, String schema) throws RemoteException {
		return getServerInfo().getServer().remindInit(login, schema);
	}
	
	@Override
	public IUser remind(String verification, String schema) throws RemoteException {
		return getServerInfo().getServer().remind(verification, schema);
	}
	
	@Override
	public IUser changePassword(String verification, String password, String schema) throws RemoteException {
		return getServerInfo().getServer().changeUserPassword(verification, password, schema);
	}
	
	@Override
	public ISession login(LoginParameters loginParameters, String password) throws RemoteException {
		IServerInfo serverInfo = getServerInfo();
		IApplicationServer loginServer = serverInfo.getServer();

		if(password == null)
			password = "";

		IUser user;
		// backward compatibility
		// if ldap flag is true and login is not in the ignore list and login is checked
		if (checkLdapLogin && !StringUtils.containsIgnoreCase(ldapUsersIgnore, loginParameters.getLogin())
				&& LdapAPI.isUserExist(new LdapAPI.Connection(), loginParameters.getLogin())) {
			try {
				user = loginServer.user(loginParameters, null);
			} catch (UserNotFoundException e) {
				if (ldapUsersCreateOnSuccessfulLogin)
					user = loginServer.create(loginParameters);
				else
					throw new AccessDeniedException();
			}
		} else {
			try {
				user = loginServer.user(loginParameters, clientHashPassword ? password : Digest.md5(password));
			} catch (UserNotFoundException ignored) {
				throw new AccessDeniedException();
			}
		}

		ISession session = sessionManager.create(user);
		session.setServerInfo(serverInfo);
		return session;
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
		ISession session = sessionManager.create(user);
		session.setServerInfo(serverInfo);
		return session;
	}

	private IServerInfo getServerInfo() throws RemoteException {
		IServerInfo serverInfo = findServer((String)null);

		if(serverInfo == null)
			throw new AccessDeniedException();
		return serverInfo;
	}

	@Override
	public ISession server(String sessionId, String serverId) throws RemoteException {
		ISession session = sessionManager.systemSession(sessionId);
		IServerInfo server = findServer(serverId);

		if(server == null)
			return null;

		session.setServerInfo(server);
		return session;
	}

	@Override
	public void userChanged(guid user, String schema) {
		sessionManager.dropUserSessions(user, schema);
	}

	@Override
	public void roleChanged(guid role, String schema) {
		sessionManager.dropRoleSessions(role, schema);
	}

	private IServerInfo findServer(String serverId) throws RemoteException {
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

		return null;
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
