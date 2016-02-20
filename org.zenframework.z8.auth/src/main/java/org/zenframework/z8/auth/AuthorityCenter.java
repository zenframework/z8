package org.zenframework.z8.auth;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.zenframework.z8.auth.loginmodule.Z8Principal;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.config.SystemProperty;
import org.zenframework.z8.server.engine.Database;
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

	static private String LoginContext = "Z8LoginContext";

	static private AuthorityCenter instance;

	private ServerConfig config;
	private Database database;

	private List<ServerInfo> servers = Collections
	        .synchronizedList(new ArrayList<ServerInfo>());

	private UserManager userManager;
	private SessionManager sessionManager;

	public AuthorityCenter(ServerConfig config) throws RemoteException {
		super(config.getAuthorityCenterPort(), IAuthorityCenter.Name);

		this.config = config;
		database = new Database(config);

		System.setProperty(SystemProperty.RAAS, config.getWorkingPath()
		        + System.getProperty("file.separator") + "raas.config");

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

		Trace.logEvent("AC: authority center started on " + netAddress()
		        + "; DB schema: " + database.schema());
	}

	@Override
    public void stop() throws RemoteException {
		for (ServerInfo info : servers.toArray(new ServerInfo[servers.size()])) {
			try {
				info.getServer().stop();
			} catch (RemoteException e) {
			}
		}

		sessionManager.stop();

		try {
			super.stop();
		} catch (RemoteException e) {
			Trace.logError(e);
		}

		Trace.logEvent("AC: authority center stopped - " + netAddress());
	}

	@Override
    public synchronized void register(IServer server) throws RemoteException {
		ServerInfo info = new ServerInfo(server, server.id(), server.netAddress());
		servers.add(info);
		Trace.logError("AC: application server has been started at " + info.getAddress(), null);
	}

	@Override
    public synchronized void unregister(IServer server) throws RemoteException {
		for (ServerInfo info : servers.toArray(new ServerInfo[servers.size()])) {
			if (info.getServer().equals(server)) {
				servers.remove(info);
				Trace.logError("AC: application server has been stopped - " + info.getAddress(), null);
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

	public void checkCredentials(String login, String password)
	        throws AccessDeniedException {
		if (login.isEmpty()
		        || login.length() > IAuthorityCenter.MaxLoginLength
		        || (password != null && password.length() > IAuthorityCenter.MaxPasswordLength)) {
			throw new AccessDeniedException();
		}

		try {
			Subject subject = new Subject();
			subject.getPrincipals().add(new Z8Principal(login, password));

			LoginContext loginContext = new LoginContext(LoginContext, subject);
			loginContext.login();
			loginContext.logout();
		} catch (LoginException e) {
			throw new AccessDeniedException();
		}
	}

	@Override
    public ISession login(String login, String password) throws RemoteException {
		IUser user = User.system();

		if (database.isSystemInstalled()) {
			checkCredentials(login, password);
			user = userManager.get(login);
		}

		if (user == null) {
			throw new AccessDeniedException();
		}

		Session session = sessionManager.create(user);

        if(servers.size() != 0)
            return getServer(session.id());

        return null;
	}

	@Override
    public ISession getTrustedSession(String userName) throws RemoteException {
		IUser user = User.system();
		if (database.isSystemInstalled()) {
			user = User.load(userName, null, true, database());
		}
		if (user == null) {
			throw new AccessDeniedException();
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
    public synchronized ISession getServer(String sessionId, String serverId)
	        throws RemoteException {
		Session session = sessionManager.get(sessionId);

		for (ServerInfo info : servers) {
			if (info.getId().equals(serverId)) {
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
