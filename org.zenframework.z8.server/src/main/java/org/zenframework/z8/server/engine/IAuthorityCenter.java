package org.zenframework.z8.server.engine;

import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.LoginParameters;
import org.zenframework.z8.server.types.guid;

import java.rmi.RemoteException;

public interface IAuthorityCenter extends IHubServer {
	int MaxLoginLength = 128;
	int MaxPasswordLength = 32;

	public IUser register(LoginParameters loginParameters, String password, String requestHost) throws RemoteException;
	public IUser verify(String verification, String schema, String requestHost) throws RemoteException;
	public IUser remindInit(String login, String schema, String requestHost) throws RemoteException;
	public IUser remind(String verification, String schema, String requestHost) throws RemoteException;
	public IUser changePassword(String verification, String password, String schema, String requestHost) throws RemoteException;
	public ISession login(LoginParameters loginParameters, String password) throws RemoteException;
	public ISession trustedLogin(LoginParameters loginParameters, boolean createIfNotExist) throws RemoteException;
	public ISession server(String session, String server) throws RemoteException;

	public void userChanged(guid user, String schema) throws RemoteException;
	public void roleChanged(guid role, String schema) throws RemoteException;
}
