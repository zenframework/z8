package org.zenframework.z8.auth;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.zenframework.z8.server.config.ServerConfig;
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

public class AuthorityCenter extends RmiServer implements IAuthorityCenter {

	private static final long serialVersionUID = -3444119932500940143L;

	private static ServerConfig Config;

	private final transient List<ServerInfo> servers = Collections.synchronizedList(new LinkedList<ServerInfo>());

	private transient UserManager userManager;
	private transient SessionManager sessionManager;

	private AuthorityCenter(int unicastPort) throws RemoteException {
		super(unicastPort, IAuthorityCenter.class);
	}

	public static void start(ServerConfig config) throws RemoteException {
		if(Config == null) {
			Config = config;
			new AuthorityCenter(Config.getUnicastAuthorityCenterPort()).start();
		}
	}

	@Override
	public void start() throws RemoteException {
		super.start();

		userManager = new UserManager(Config);

		sessionManager = new SessionManager();
		sessionManager.start(Config);

		Trace.logEvent("AC: authority center started at '" + getUrl() + "'");
	}

	@Override
	public void stop() throws RemoteException {
		sessionManager.stop();

		for(ServerInfo info : servers.toArray(new ServerInfo[0])) {
			try {
				info.getServer().stop();
			} catch(RemoteException e) {
			}
		}

		try {
			super.stop();
		} catch(RemoteException e) {
			Trace.logError(e);
		}

		Trace.logEvent("AC: authority center has been stopped at '" + getUrl() + "'");
	}

	@Override
	public synchronized void register(IServer server) throws RemoteException {
		ServerInfo info = new ServerInfo(server, server.id(), server.getUrl());
		servers.add(info);
		Trace.logEvent("AC: application server started at '" + info.getUrl() + "'");
	}

	@Override
	public synchronized void unregister(IServer server) throws RemoteException {
		for(ServerInfo info : servers.toArray(new ServerInfo[servers.size()])) {
			if(info.getServer().equals(server)) {
				servers.remove(info);
				Trace.logEvent("AC: application server has been stopped at '" + info.getUrl() + "'");
				break;
			}
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

	@Override
	public synchronized ISession getServer(String sessionId, String serverId) throws RemoteException {
		Session session = sessionManager.get(sessionId);
		ServerInfo server = findServer(serverId);

		if(server == null)
			return null;

		session.setServerInfo(server);
		return session;
	}

	private ISession doLogin(String login, String password) throws RemoteException {
		ServerInfo serverInfo = findServer(null);

		if(serverInfo == null)
			throw new AccessDeniedException();

		IApplicationServer loginServer = serverInfo.getApplicationServer();
		IUser user = password != null ? loginServer.login(login, password) : loginServer.login(login);
		userManager.add(user);

		Session session = sessionManager.create(user);
		session.setServerInfo(serverInfo);
		return session;
	}

	private boolean isAlive(ServerInfo server) throws RemoteException {
		try {
			server.getServer().id();
			return true;
		} catch(ConnectException e) {
			return false;
		}
	}

	private ServerInfo findServer(String serverId) throws RemoteException {
		ServerInfo[] servers = this.servers.toArray(new ServerInfo[0]);

		for(ServerInfo server : servers) {
			if(!isAlive(server)) {
				unregister(server.getServer());
				continue;
			}

			if(serverId == null || server.getId().equals(serverId)) {
				if(serverId == null && this.servers.size() > 1) {
					this.servers.remove(server);
					this.servers.add(server);
				}

				return server;
			}
		}

		return null;
	}

}
