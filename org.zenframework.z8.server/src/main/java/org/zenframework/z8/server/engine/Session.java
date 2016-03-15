package org.zenframework.z8.server.engine;

import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.User;

public class Session implements ISession {
    private static final long serialVersionUID = -6111053710062684661L;

    private String id;
    private IUser user;
    private ServerInfo serverInfo;

    private long createdAt;
    private long lastAccessTime;

    public Session() {
        this("system", User.system());
    }

    public Session(String id, IUser user) {
        this.id = id;
        this.user = user;
        this.createdAt = System.currentTimeMillis();

        access();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public IUser user() {
        return user;
    }

    @Override
    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    @Override
    public long getTimeCreate() {
        return createdAt;
    }

    @Override
    public synchronized long getLastAccessTime() {
        return lastAccessTime;
    }

    public synchronized void access() {
        lastAccessTime = System.currentTimeMillis();
    }
}
