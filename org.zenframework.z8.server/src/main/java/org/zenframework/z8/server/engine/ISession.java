package org.zenframework.z8.server.engine;

import java.io.Serializable;

import org.zenframework.z8.server.security.IUser;

public interface ISession extends Serializable {
    public String id();

    public IUser user();

    public ServerInfo getServerInfo();

    public Database database();

    public void setDatabase(Database database);

    public long getTimeCreate();

    public long getLastAccessTime();
}
