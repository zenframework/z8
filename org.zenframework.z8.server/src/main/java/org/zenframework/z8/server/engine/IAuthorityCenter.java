package org.zenframework.z8.server.engine;

import org.zenframework.z8.server.security.User;
import org.zenframework.z8.server.security.LoginParameters;
import org.zenframework.z8.server.types.guid;

import java.rmi.RemoteException;

public interface IAuthorityCenter extends IHubServer {
	int MaxLoginLength = 128;
	int MaxPasswordLength = 32;

	public User register(LoginParameters loginParameters, String password, String requestHost) throws RemoteException;
	public User verify(String verification, String schema, String requestHost) throws RemoteException;
	public User remindInit(String login, String schema, String requestHost) throws RemoteException;
	public User remind(String verification, String schema, String requestHost) throws RemoteException;
	public User changePassword(String verification, String password, String schema, String requestHost) throws RemoteException;

	public Session login(LoginParameters loginParameters, String password) throws RemoteException;
	public Session trustedLogin(LoginParameters loginParameters, boolean createIfNotExist) throws RemoteException;
	public Session server(String session, String server) throws RemoteException;

	public void userChanged(guid user, String schema) throws RemoteException;
	public void roleChanged(guid role, String schema) throws RemoteException;
}
