package org.zenframework.z8.auth;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.engine.Session;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.security.IAccount;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.User;
import org.zenframework.z8.server.types.guid;

public class SessionManager {
	private long sessionTimeout = 0;

	private Map<String, ISession> systemSessions = new HashMap<String, ISession>();
	private Map<String, ISession> siteSessions = new HashMap<String, ISession>();

	private Map<guid, ISession> users = new HashMap<guid, ISession>();
	private Map<guid, ISession> accounts = new HashMap<guid, ISession>();

	static private ISession siteSession = new Session("site", User.site());

	SessionManager() {
	}

	public void start() {
		setSessionTimeout(ServerConfig.sessionTimeout());
	}

	public void stop() {
	}

	public ISession systemSession(String id) {
		ISession session = systemSessions.get(id);

		if(session != null) {
			session.access();
			return new Session(session);
		}

		throw new AccessDeniedException();
	}

	public ISession siteSession(String id) {
		if(id == null)
			return siteSession;

		ISession session = siteSessions.get(id);

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
			systemSessions.put(sessionId, session);
			users.put(userId, session);
		} else
			session.setUser(user);

		return session;
	}

	synchronized public ISession create(IAccount account) {
		guid accountId = account.id();
		ISession session = accounts.get(accountId);

		if(session == null) {
			String sessionId = guid.create().toString();
			session = new Session(sessionId, account);
			siteSessions.put(sessionId, session);
			accounts.put(accountId, session);
		} else
			session.setAccount(account);

		return session;
	}

	public void check() {
		if(sessionTimeout == 0)
			return;

		long timeLimit = System.currentTimeMillis() - sessionTimeout;

		for(ISession session : systemSessions.values().toArray(new ISession[0])) {
			if(session.getLastAccessTime() < timeLimit) {
				synchronized(this) {
					users.remove(session.user().id());
					systemSessions.remove(session.id());
				}
			}
		}

		for(ISession session : siteSessions.values().toArray(new ISession[0])) {
			if(session.getLastAccessTime() < timeLimit) {
				synchronized(this) {
					accounts.remove(session.user().id());
					siteSessions.remove(session.id());
				}
			}
		}
	}

	private void setSessionTimeout(int timeout) {
		sessionTimeout = timeout * 60 * 1000;
	}
}
