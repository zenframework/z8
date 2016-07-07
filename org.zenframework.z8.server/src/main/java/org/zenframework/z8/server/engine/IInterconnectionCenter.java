package org.zenframework.z8.server.engine;

import java.rmi.RemoteException;

public interface IInterconnectionCenter extends IServer {
	void register(IApplicationServer server) throws RemoteException;
	void unregister(IApplicationServer server) throws RemoteException;

	IApplicationServer connect(String domain) throws RemoteException;
}
