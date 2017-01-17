package org.zenframework.z8.server.engine;

import java.rmi.RemoteException;

public interface IAuthorityCenter extends IHubServer {
	int MaxLoginLength = 32;
	int MaxPasswordLength = 32;

	public ISession login(String login, String password) throws RemoteException;
	public ISession server(String session, String server) throws RemoteException;

	public ISession siteLogin(String login, String password) throws RemoteException;
	public ISession siteServer(String session, String server) throws RemoteException;
}
