package org.zenframework.z8.auth;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.Session;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.types.guid;

public class SessionManager {
	private long sessionTimeout = 0;

	private Map<String, Session> sessions = new HashMap<String, Session>();

	SessionManager() {
	}

	public void start() {
		setSessionTimeout(ServerConfig.sessionTimeout());
	}

	public void stop() {
	}

	public Session get(String id) {
		Session session = sessions.get(id);

		if(session != null) {
			session.access();
			return new Session(session);
		}

		throw new AccessDeniedException();
	}

	synchronized public Session create(IUser user) {
		String id = guid.create().toString();
		Session session = new Session(id, user);
		sessions.put(id, session);
		return session;
	}

	synchronized private void drop(String id) {
		Session session = sessions.get(id);

		if(session != null)
			sessions.remove(id);
	}

	public void check() {
		if(sessionTimeout != 0) {
			long timeLimit = System.currentTimeMillis() - sessionTimeout;

			for(Session session : sessions.values().toArray(new Session[0])) {
				if(session.getLastAccessTime() < timeLimit)
					drop(session.id());
			}
		}
	}

	private void setSessionTimeout(int timeout) {
		sessionTimeout = timeout * 60 * 1000;
	}
}
