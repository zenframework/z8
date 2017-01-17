package org.zenframework.z8.server.engine;

import java.io.Serializable;

import org.zenframework.z8.server.security.IAccount;
import org.zenframework.z8.server.security.IUser;

public interface ISession extends RmiSerializable, Serializable {
	public String id();

	public IUser user();
	public void setUser(IUser user);

	public IAccount account();
	public void setAccount(IAccount user);

	public IServerInfo getServerInfo();
	public void setServerInfo(IServerInfo serverInfo);

	public void access();
	public long getLastAccessTime();
}
