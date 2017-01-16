package org.zenframework.z8.auth;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.engine.Session;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.types.guid;

public class SessionManager {
	private long sessionTimeout = 0;

	private Map<String, ISession> sessions = new HashMap<String, ISession>();
	private Map<guid, ISession> users = new HashMap<guid, ISession>();

	SessionManager() {
	}

	public void start() {
		setSessionTimeout(ServerConfig.sessionTimeout());
	}

	public void stop() {
	}

	public Session get(String id) {
		ISession session = sessions.get(id);

		if(session != null) {
			session.access();
			return new Session(session);
		}

		throw new AccessDeniedException();
	}

	synchronized public ISession create(IUser user) {
		guid userId = user.id();
		ISession session = users.get(userId);

		if(session == null) {
			String sessionId = guid.create().toString();
			session = new Session(sessionId, user);
			sessions.put(sessionId, session);
			users.put(userId, session);
		} else
			session.setUser(user);

		return session;
	}

	synchronized private void drop(String id) {
		ISession session = sessions.get(id);

		if(session != null) {
			users.remove(session.user().id());
			sessions.remove(id);
		}
	}

	public void check() {
		if(sessionTimeout != 0) {
			long timeLimit = System.currentTimeMillis() - sessionTimeout;

			for(ISession session : sessions.values().toArray(new ISession[0])) {
				if(session.getLastAccessTime() < timeLimit)
					drop(session.id());
			}
		}
	}

	private void setSessionTimeout(int timeout) {
		sessionTimeout = timeout * 60 * 1000;
	}
}
