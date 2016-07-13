package org.zenframework.z8.server.engine;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;

public interface IServerInfo extends RmiSerializable, Serializable {
	public Proxy getProxy();

	public IApplicationServer getServer();
	public void setServer(IApplicationServer server);

	public String getId();
	public void setId(String id);

	public String[] getDomains();
	public void setDomains(String[] domains);

	public boolean isAlive() throws RemoteException;	// temporary unavailable
	public boolean isDead() throws RemoteException;		// dead
}
