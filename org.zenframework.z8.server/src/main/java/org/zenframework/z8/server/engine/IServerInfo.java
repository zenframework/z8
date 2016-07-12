package org.zenframework.z8.server.engine;

import java.io.Serializable;
import java.rmi.RemoteException;

public interface IServerInfo extends RmiSerializable, Serializable {
	public IApplicationServer getServer();

	public String getId();

	public String[] getDomains();

	public boolean isAlive() throws RemoteException;	// temporary unavailable
	public boolean isDead() throws RemoteException;		// dead
}
