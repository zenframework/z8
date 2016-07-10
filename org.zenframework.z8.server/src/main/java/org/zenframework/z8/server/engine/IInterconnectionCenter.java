package org.zenframework.z8.server.engine;

import java.rmi.RemoteException;

public interface IInterconnectionCenter extends IHubServer {
	IApplicationServer connect(String domain) throws RemoteException;
}
