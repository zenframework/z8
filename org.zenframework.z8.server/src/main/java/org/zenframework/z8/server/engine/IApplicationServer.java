package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Map;

import org.zenframework.z8.server.base.xml.GNode;
import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.LoginParameters;
import org.zenframework.z8.server.types.file;

public interface IApplicationServer extends IServer {
	GNode processRequest(ISession session, GNode request) throws RemoteException;

	file download(ISession session, GNode request, file file) throws RemoteException, IOException;

	IUser registerUser(LoginParameters loginParameters, String password, String requestHost) throws RemoteException;
	IUser verifyUser(String verification, String schema, String requestHost) throws RemoteException;
	IUser remindInit(String login, String schema, String requestHost) throws RemoteException;
	IUser remind(String verification, String schema, String requestHost) throws RemoteException;
	IUser changeUserPassword(String verification, String password, String schema, String requestHost) throws RemoteException;
	IUser user(LoginParameters loginParameters, String password) throws RemoteException;
	IUser create(LoginParameters loginParameters) throws RemoteException;

	String[] domains() throws RemoteException;
	Map<String, String> settings() throws RemoteException;
	Map<String, String> properties() throws RemoteException;

	boolean has(Message message) throws RemoteException;
	boolean accept(Message message) throws RemoteException;
}
