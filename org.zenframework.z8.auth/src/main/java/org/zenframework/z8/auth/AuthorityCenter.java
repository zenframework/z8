package org.zenframework.z8.auth;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.crypto.MD5;
import org.zenframework.z8.server.engine.HubServer;
import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.engine.IAuthorityCenter;
import org.zenframework.z8.server.engine.IInterconnectionCenter;
import org.zenframework.z8.server.engine.IServerInfo;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.engine.ServerInfo;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.ldap.ActiveDirectory;
import org.zenframework.z8.server.ldap.LdapUser;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.RequestDispatcher;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.utils.StringUtils;

public class AuthorityCenter extends HubServer implements IAuthorityCenter {
	private static final String serversCache = "authority.center.cache";

	static public String id = guid.create().toString();

	static private AuthorityCenter instance = null;

	private final boolean clientHashPassword;
	private final String ldapUrl;
	private final boolean checkLdapLogin;
	private final String ldapDefaultDomain;
	private Collection<String> ldapUsersIgnore;
	private boolean ldapUsersCreateOnSuccessfulLogin;
	private boolean cacheEnabled;

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
		ldapDefaultDomain = ServerConfig.ldapDefaultDomain();
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
	public ISession login(String login, String password) throws RemoteException {
		IServerInfo serverInfo = findServer((String)null);

		if(serverInfo == null)
			throw new AccessDeniedException();

		IApplicationServer loginServer = serverInfo.getServer();

		ISession session = null;

		if(password == null)
			password = "";

		IUser user;
		if (checkLdapLogin && !StringUtils.containsIgnoreCase(ldapUsersIgnore, login)) {
			try {
				ActiveDirectory activeDirectory = new ActiveDirectory();
				activeDirectory.searchUser(
						ServerConfig.searchBase(), String.format(ServerConfig.searchFilter(), login));
				activeDirectory.close();
			} catch (NamingException e) {
				Trace.logError("Failed to get user attributes from active directory service", e);
				throw new AccessDeniedException();
			}
			user = loginServer.userLoad(login, ldapUsersCreateOnSuccessfulLogin);
		} else {
			user = loginServer.user(login, clientHashPassword ? password : MD5.hex(password), false);
		}
		session = sessionManager.create(user);

		session.setServerInfo(serverInfo);
		return session;
	}

	/**
	 * @param principalName The string constructs from user name and his/her realm, like: userLogin@companyDomain.com
	 *                      https://tools.ietf.org/html/rfc6806
	 */
	@Override
	public ISession ssoAuth(String principalName) throws RemoteException {
		IServerInfo serverInfo = findServer((String)null);

		if(serverInfo == null || principalName == null)
			throw new AccessDeniedException();

		IApplicationServer loginServer = serverInfo.getServer();

		IUser user;
		String login = principalName.contains("@") ? principalName.split("@")[0] : principalName;
		try {
			user = loginServer.userLoad(login, false);
		} catch (AccessDeniedException e) {
			user = createUser(loginServer, principalName);
		}
		if (user == null) {
			throw new AccessDeniedException();
		}
		ISession session = sessionManager.create(user);
		session.setServerInfo(serverInfo);
		return session;
	}

	/**
	 * Getting user information from active directory and creating a new user
	 */
	private IUser createUser(IApplicationServer loginServer, String principalName) throws RemoteException{
		LdapUser ldapUser;
		try {
			ActiveDirectory activeDirectory = new ActiveDirectory();
			ldapUser = activeDirectory.searchUser(
					ServerConfig.searchBase(), String.format(ServerConfig.searchFilter(), principalName));
			activeDirectory.close();
		} catch (NamingException e) {
			Trace.logError("Failed to get user attributes from active directory service", e);
			return null;
		}
		// TODO need the way to pass user groups ldapUser.getMemberOf(), as I understood BL does not have primitive array type
		IUser user = loginServer.createUser(
				ldapUser.getLogin(),
				ldapUser.getEmail(),
				ldapUser.getFullName(),
				ldapUser.getParameters()
		);
		return user;
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
	public void userChanged(guid user) {
		sessionManager.dropUserSessions(user);
	}

	@Override
	public void roleChanged(guid role) {
		sessionManager.dropRoleSessions(role);
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
