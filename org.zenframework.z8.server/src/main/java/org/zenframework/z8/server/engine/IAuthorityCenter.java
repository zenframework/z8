package org.zenframework.z8.server.engine;

import java.rmi.RemoteException;

public interface IAuthorityCenter extends IHubServer {
	int MaxLoginLength = 32;
	int MaxPasswordLength = 32;

	ISession login(String userName) throws RemoteException;

	ISession login(String userName, String password) throws RemoteException;

	ISession getServer(String sessionId, String serverId) throws RemoteException;
}
