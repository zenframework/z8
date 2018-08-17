
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
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.RequestDispatcher;
import org.zenframework.z8.server.security.IAccount;
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
	private final String ldapDefaultDomain;
	private final Collection<String> ldapIgnoreUsers;

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
		clientHashPassword = ServerConfig.clientHashPassword();
		ldapUrl = ServerConfig.ldapUrl();
		ldapDefaultDomain = ServerConfig.ldapDefaultDomain();
		ldapIgnoreUsers = ServerConfig.ldapIgnoreUsers();
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
		return login(login, password, false);
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
	public ISession siteLogin(String login, String password) throws RemoteException {
		return login(login, password, true);
	}

	@Override
	public ISession siteServer(String sessionId, String serverId) throws RemoteException {
		ISession session = sessionManager.siteSession(sessionId);
		IServerInfo server = findServer(serverId);

		if(server == null)
			return null;

		session.setServerInfo(server);
		return session;
	}

	private ISession login(String login, String password, boolean site) throws RemoteException {
		IServerInfo serverInfo = findServer((String)null);

		if(serverInfo == null)
			throw new AccessDeniedException();

		IApplicationServer loginServer = serverInfo.getServer();

		ISession session = null;
		
		if(password == null)
			password = "";

		if(site) {
			IAccount account = loginServer.account(login, password);
			session = sessionManager.create(account);
		} else {
			if (!ldapUrl.isEmpty() && !StringUtils.containsIgnoreCase(ldapIgnoreUsers, login)) {
				checkLdapLogin(login, password);
				password = "";
			}
			IUser user = loginServer.user(login, clientHashPassword ? password : MD5.hex(password));
			session = sessionManager.create(user);
		}

		session.setServerInfo(serverInfo);
		return session;
	}

	private void checkLdapLogin(String login, String password) {
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
		return new File(Folders.Base, serversCache);
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
