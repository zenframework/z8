package org.zenframework.z8.server.engine;

import java.io.Serializable;

import org.zenframework.z8.server.security.IUser;

public interface ISession extends RmiSerializable, Serializable {
    public String id();

    public IUser user();

    public IServerInfo getServerInfo();

    public long getLastAccessTime();
}
