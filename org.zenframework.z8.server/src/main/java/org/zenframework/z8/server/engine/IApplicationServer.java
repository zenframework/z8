package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.rmi.RemoteException;

import org.zenframework.z8.server.base.xml.GNode;
import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.security.LoginParameters;
import org.zenframework.z8.server.security.User;
import org.zenframework.z8.server.types.file;

public interface IApplicationServer extends IServer {
	public GNode processRequest(Session session, GNode request) throws RemoteException;

	public file download(Session session, GNode request, file file) throws RemoteException, IOException;

	public User registerUser(LoginParameters loginParameters, String password, String requestHost) throws RemoteException;
	public User verifyUser(String verification, String schema, String requestHost) throws RemoteException;
	public User remindInit(String login, String schema, String requestHost) throws RemoteException;
	public User remind(String verification, String schema, String requestHost) throws RemoteException;
	public User changeUserPassword(String verification, String password, String schema, String requestHost) throws RemoteException;

	public User user(LoginParameters loginParameters, String password) throws RemoteException;
	public User create(LoginParameters loginParameters) throws RemoteException;
	

	public String[] domains() throws RemoteException;

	public boolean has(Message message) throws RemoteException;
	public boolean accept(Message message) throws RemoteException;
}
