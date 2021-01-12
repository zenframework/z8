package org.zenframework.z8.auth;

import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.crypto.MD5;
import org.zenframework.z8.server.engine.*;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.RequestDispatcher;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.utils.StringUtils;

import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Hashtable;

public class AuthorityCenter extends HubServer implements IAuthorityCenter {
	private static final String serversCache = "authority.center.cache";

	static public String id = guid.create().toString();

	static private AuthorityCenter instance = null;

	private final boolean clientHashPassword;
	private final String ldapUrl;
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
	public ISession login(String login, String password, String scheme) throws RemoteException {
		IServerInfo serverInfo = findServer((String)null);

		if(serverInfo == null)
			throw new AccessDeniedException();

		IApplicationServer loginServer = serverInfo.getServer();

		ISession session = null;

		if(password == null)
			password = "";

		if (!ldapUrl.isEmpty() && !StringUtils.containsIgnoreCase(ldapUsersIgnore, login)) {
			checkLdapLogin(login, password);
			password = "";
		}

		IUser user = loginServer.user(login, clientHashPassword ? password : MD5.hex(password), scheme, !ldapUrl.isEmpty() && ldapUsersCreateOnSuccessfulLogin);
		session = sessionManager.create(user);

		session.setServerInfo(serverInfo);
		return session;
	}

	@Override
	public ISession login(String login, String scheme) throws RemoteException {
		IServerInfo serverInfo = findServer((String)null);

		if(serverInfo == null)
			throw new AccessDeniedException();

		IApplicationServer loginServer = serverInfo.getServer();


		IUser user = loginServer.user(login, null, scheme, false);
		ISession session = sessionManager.create(user);

		session.setServerInfo(serverInfo);
		return session;
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

	private void checkLdapLogin(String login, String password) {
		if (password.isEmpty())
			throw new AccessDeniedException();
		Hashtable<String, String> environment = new Hashtable<String, String>();
		environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		environment.put(Context.PROVIDER_URL, ldapUrl);
		environment.put(Context.SECURITY_AUTHENTICATION, "simple");
		environment.put(Context.SECURITY_PRINCIPAL, (!login.contains("\\") && !ldapDefaultDomain.isEmpty()
				? ldapDefaultDomain + '\\' : "") + login);
		environment.put(Context.SECURITY_CREDENTIALS, password);
		try {
			DirContext context = new InitialDirContext(environment);
			context.close();
		} catch (AuthenticationNotSupportedException e) {
			Trace.logError("The authentication method is not supported by the server", e);
			throw new AccessDeniedException();
		} catch (AuthenticationException e) {
			throw new AccessDeniedException();
		} catch (NamingException e) {
			Trace.logError("Error when trying to create the context", e);
			throw new AccessDeniedException();
		}
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
