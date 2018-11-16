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
	private Map<guid, ISession> users = new HashMap<guid, ISession>();

	SessionManager() {
	}

	public void start() {
		setSessionTimeout(ServerConfig.sessionTimeout());
	}

	public void stop() {
	}

	public ISession systemSession(String id) {
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

	synchronized public void dropUserSessions(guid user) {
		ISession session = users.get(user);

		if(session != null) {
			users.remove(user);
			sessions.remove(session.id());
		}
	}

	synchronized public void dropRoleSessions(guid roleId) {
		for(ISession session : users.values().toArray(new ISession[0])) {
			IUser user = session.user();
			if(user.isAdministrator())
				continue;

			for(IRole role : user.roles()) {
				if(role.id().equals(roleId)) {
					users.remove(user.id());
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
					users.remove(session.user().id());
					sessions.remove(session.id());
				}
			}
		}
	}

	private void setSessionTimeout(int timeout) {
		sessionTimeout = timeout * 60 * 1000;
	}
}
