package org.zenframework.z8.server.engine;

import java.rmi.RemoteException;

import org.zenframework.z8.server.types.guid;

public interface IAuthorityCenter extends IHubServer {
	int MaxLoginLength = 32;
	int MaxPasswordLength = 32;

	public ISession login(String login, String password) throws RemoteException;
	public ISession server(String session, String server) throws RemoteException;

	public void userChanged(guid user) throws RemoteException;
	public void roleChanged(guid role) throws RemoteException;
}
