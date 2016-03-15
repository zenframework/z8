package org.zenframework.z8.auth;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.config.SystemProperty;
import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.engine.IAuthorityCenter;
import org.zenframework.z8.server.engine.IServer;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.engine.RmiServer;
import org.zenframework.z8.server.engine.ServerInfo;
import org.zenframework.z8.server.engine.Session;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.User;

public class AuthorityCenter extends RmiServer implements IAuthorityCenter {

	private static final long serialVersionUID = -3444119932500940143L;

	static private AuthorityCenter instance;

	private ServerConfig config;
	private Database database;

	private List<ServerInfo> servers = Collections.synchronizedList(new ArrayList<ServerInfo>());

	private UserManager userManager;
	private SessionManager sessionManager;

	public AuthorityCenter(ServerConfig config) throws RemoteException {
		super(config.getAuthorityCenterPort(), IAuthorityCenter.Name);

		this.config = config;
		database = new Database(config);

		System.setProperty(SystemProperty.RAAS, config.getWorkingPath() + System.getProperty("file.separator") + "raas.config");

		instance = this;

		start();
	}

	static public ServerConfig config() {
		return instance.config;
	}

	static public AuthorityCenter get() {
		return instance;
	}

	@Override
	public void start() throws RemoteException {
		super.start();

		userManager = new UserManager(config);

		sessionManager = new SessionManager();
		sessionManager.start(config);

		Trace.logEvent("AC: authority center started at '" + netAddress() + "'");
	}

	@Override
	public void stop() throws RemoteException {
		for(ServerInfo info : servers.toArray(new ServerInfo[servers.size()])) {
			try {
				info.getServer().stop();
			} catch(RemoteException e) {
			}
		}

		sessionManager.stop();

		try {
			super.stop();
		} catch(RemoteException e) {
			Trace.logError(e);
		}

		Trace.logEvent("AC: authority center has been stopped at '" + netAddress() + "'");
	}

	@Override
	public synchronized void register(IServer server) throws RemoteException {
		ServerInfo info = new ServerInfo(server, server.id(), server.netAddress());
		servers.add(info);
		Trace.logEvent("AC: application server started at '" + info.getAddress() + "'");
	}

	@Override
	public synchronized void unregister(IServer server) throws RemoteException {
		for(ServerInfo info : servers.toArray(new ServerInfo[servers.size()])) {
			if(info.getServer().equals(server)) {
				servers.remove(info);
				Trace.logEvent("AC: application server has been stopped at '" + info.getAddress() + "'");
				break;
			}
		}
	}

	static public Database database() {
		return instance.database;
	}

	static public UserManager getUserManager() {
		return instance.userManager;
	}

	private IApplicationServer getLoginServer() {
		try {
			return instance.servers.get(0).getApplicationServer();
		} catch(Throwable e) {
			throw new AccessDeniedException();
		}
	}
	
	@Override
	public ISession login(String login) throws RemoteException {
		return doLogin(login, null);
	}


	@Override
	public ISession login(String login, String password) throws RemoteException {
		if(password == null)
			throw new AccessDeniedException();
		
		return doLogin(login, password);
	}

	private ISession doLogin(String login, String password) throws RemoteException {
		IUser user = User.system();

		if(database.isSystemInstalled()) {
			IApplicationServer loginServer = getLoginServer();
			user = password != null ? loginServer.login(login, password) : loginServer.login(login);
			AuthorityCenter.getUserManager().add(user);
		}

		Session session = sessionManager.create(user);

		if(servers.size() != 0)
			return getServer(session.id());

		return null;
	}

	@Override
	public synchronized ISession getServer(String sessionId) throws RemoteException {
		Session session = sessionManager.get(sessionId);

		if(servers.size() == 0)
			return null;

		ServerInfo info = servers.get(0);

		if(servers.size() > 1) {
			servers.remove(info);
			servers.add(info);
		}

		session.setServerInfo(info);
		return session;
	}

	@Override
	public synchronized ISession getServer(String sessionId, String serverId) throws RemoteException {
		Session session = sessionManager.get(sessionId);

		for(ServerInfo info : servers) {
			if(info.getId().equals(serverId)) {
				session.setServerInfo(info);
				return session;
			}
		}

		return null;
	}

	@Override
	public void save(IUser user) throws RemoteException {
		user.save(database);
	}

}
