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
import org.zenframework.z8.server.engine.ServerInfo;
import org.zenframework.z8.server.engine.Session;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.exceptions.UserNotFoundException;
import org.zenframework.z8.server.ldap.LdapAPI;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.RequestDispatcher;
import org.zenframework.z8.server.security.LoginParameters;
import org.zenframework.z8.server.security.User;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.guid;

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
	public Session login(LoginParameters loginParameters, String password) throws RemoteException {
		ServerInfo serverInfo = getServerInfo();
		IApplicationServer loginServer = serverInfo.getServer();

		if(password == null)
			password = "";

		User user;
		// Что это за херня? Зачем это все? backward compatibility с чем? 
		// backward compatibility
		// if ldap flag is true and login is not in the ignore list and login is checked
		if (checkLdapLogin && !LdapAPI.containsIgnoreCase(ldapUsersIgnore, loginParameters.getLogin())
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

		Session session = sessionManager.create(user);
		session.setServerInfo(serverInfo);
		return session;
	}

	@Override
	public Session trustedLogin(LoginParameters loginParameters, boolean createIfNotExist) throws RemoteException {
		ServerInfo serverInfo = getServerInfo();
		// A это что? UserNotFoundException - серьезно?
		try {
			return login0(serverInfo, loginParameters);
		} catch (UserNotFoundException e) {
			if (!createIfNotExist)
				throw e;
			serverInfo.getServer().create(loginParameters);
			return login0(serverInfo, loginParameters);
		}
	}

	private Session login0(ServerInfo serverInfo, LoginParameters loginParameters) throws RemoteException {
		User user = serverInfo.getServer().user(loginParameters, null);
		Session session = sessionManager.create(user);
		session.setServerInfo(serverInfo);
		return session;
	}

	private ServerInfo getServerInfo() throws RemoteException {
		ServerInfo serverInfo = findServer((String)null);

		if(serverInfo == null)
			throw new AccessDeniedException();
		return serverInfo;
	}

	@Override
	public Session server(String sessionId, String serverId) throws RemoteException {
		Session session = sessionManager.systemSession(sessionId);
		ServerInfo server = findServer(serverId);

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

	private ServerInfo findServer(String serverId) throws RemoteException {
		ServerInfo[] servers = getServers();

		for(ServerInfo server : servers) {
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

			for(ServerInfo server : getServers()) {
				if(server.isAlive())
					center.register(server.getServer());
			}
		} catch(Throwable e) {
		}
	}
}
