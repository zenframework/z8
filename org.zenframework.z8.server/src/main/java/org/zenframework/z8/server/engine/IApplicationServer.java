package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.rmi.RemoteException;

import org.zenframework.z8.server.base.xml.GNode;
import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.LoginParameters;
import org.zenframework.z8.server.types.file;

public interface IApplicationServer extends IServer {
	public GNode processRequest(ISession session, GNode request) throws RemoteException;

	public file download(ISession session, GNode request, file file) throws RemoteException, IOException;

	public IUser registerUser(LoginParameters loginParameters, String password, String requestHost) throws RemoteException;
	public IUser verifyUser(String verification, String schema, String requestHost) throws RemoteException;
	public IUser remindInit(String login, String schema, String requestHost) throws RemoteException;
	public IUser remind(String verification, String schema, String requestHost) throws RemoteException;
	public IUser changeUserPassword(String verification, String password, String schema, String requestHost) throws RemoteException;
	public IUser loginUser(LoginParameters loginParameters, String password) throws RemoteException;
	public void logoutUser(ISession session) throws RemoteException;

	public IUser create(LoginParameters loginParameters) throws RemoteException;

	public String[] domains() throws RemoteException;

	public boolean has(Message message) throws RemoteException;
	public boolean accept(Message message) throws RemoteException;
}
