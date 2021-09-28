package org.zenframework.z8.auth;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.Session;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.security.Role;
import org.zenframework.z8.server.security.User;
import org.zenframework.z8.server.types.guid;

public class SessionManager {
	private long sessionTimeout = 0;

	private Map<String, Session> sessions = new HashMap<String, Session>();
	private Map<String, Session> userSessions = new HashMap<String, Session>();

	SessionManager() {
	}

	public void start() {
		setSessionTimeout(ServerConfig.sessionTimeout());
	}

	public void stop() {
	}

	public Session systemSession(String sessionId) {
		Session session = sessions.get(sessionId);

		if(session != null) {
			session.touch();
			return new Session(session);
		}

		throw new AccessDeniedException();
	}

	private String userKey(guid id, String schema) {
		return schema + '/' + id;
	}

	synchronized public Session create(User user) {
		String userId = userKey(user.getId(), user.getDatabase().getSchema());
		Session session = userSessions.get(userId);

		if(session == null) {
			String sessionId = guid.create().toString();
			session = new Session(sessionId, user);
			sessions.put(sessionId, session);
			userSessions.put(userId, session);
		} else
			session.setUser(user);

		return session;
	}

	synchronized public void dropUserSessions(guid userId, String schema) {
		String userKey = userKey(userId, schema);
		Session session = userSessions.get(userKey);

		if(session != null) {
			userSessions.remove(userKey);
			sessions.remove(session.getId());
		}
	}

	synchronized public void dropRoleSessions(guid roleId, String schema) {
		for(Session session : userSessions.values().toArray(new Session[0])) {
			User user = session.getUser();
			if(user.isAdministrator() || !user.getDatabase().getSchema().equals(schema))
				continue;

			for(Role role : user.getRoles()) {
				if(role.getId().equals(roleId)) {
					userSessions.remove(userKey(user.getId(), schema));
					sessions.remove(session.getId());
					break;
				}
			}
		}
	}

	public void check() {
		if(sessionTimeout == 0)
			return;

		long timeLimit = System.currentTimeMillis() - sessionTimeout;

		for(Session session : sessions.values().toArray(new Session[0])) {
			if(session.getLastAccessTime() < timeLimit) {
				synchronized(this) {
					User user = session.getUser();
					userSessions.remove(userKey(user.getId(), user.getDatabase().getSchema()));
					sessions.remove(session.getId());
				}
			}
		}
	}

	private void setSessionTimeout(int timeout) {
		sessionTimeout = timeout * 60 * 1000;
	}
}
