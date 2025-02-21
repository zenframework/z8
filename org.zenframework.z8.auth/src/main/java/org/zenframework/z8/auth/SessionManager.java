package org.zenframework.z8.auth;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.engine.Session;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.security.IRole;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.types.guid;

public class SessionManager {
	private long sessionTimeout = 0;

	private Map<String, ISession> sessions = new HashMap<String, ISession>();
	private Map<String, ISession> userSessions = new HashMap<String, ISession>();

	SessionManager() {}

	public void start() {
		setSessionTimeout(ServerConfig.sessionTimeout());
	}

	public void stop() {}

	public ISession getSession(String sessionId) {
		ISession session = sessions.get(sessionId);

		if (session != null)
			return new Session(session.touch());

		throw new AccessDeniedException();
	}

	private String userKey(guid id, String schema) {
		return schema + '/' + id;
	}

	synchronized public ISession create(IUser user) {
		String userId = userKey(user.getId(), user.database().schema());
		ISession session = userSessions.get(userId);

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
		ISession session = userSessions.get(userKey);

		if(session != null) {
			userSessions.remove(userKey);
			sessions.remove(session.id());
		}
	}

	synchronized public void dropRoleSessions(guid roleId, String schema) {
		for(ISession session : userSessions.values().toArray(new ISession[0])) {
			IUser user = session.user();
			if(user.isAdministrator() || !user.database().schema().equals(schema))
				continue;

			for(IRole role : user.getRoles()) {
				if(role.id().equals(roleId)) {
					userSessions.remove(userKey(user.getId(), schema));
					sessions.remove(session.id());
					break;
				}
			}
		}
	}

	public void check() {
		if(sessionTimeout == 0)
			return;

		long timeLimit = System.currentTimeMillis() - sessionTimeout;

		for(ISession session : sessions.values().toArray(new ISession[0])) {
			if(session.getLastAccessTime() < timeLimit) {
				synchronized(this) {
					IUser user = session.user();
					userSessions.remove(userKey(user.getId(), user.database().schema()));
					sessions.remove(session.id());
				}
			}
		}
	}

	private void setSessionTimeout(int timeout) {
		sessionTimeout = timeout * 60 * 1000;
	}
}
