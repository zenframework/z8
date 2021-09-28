package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.rmi.RemoteException;

import org.zenframework.z8.server.base.xml.GNode;
import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.security.User;
import org.zenframework.z8.server.types.file;

public interface IApplicationServer extends IServer {
	public GNode processRequest(Session session, GNode request) throws RemoteException;

	public file download(Session session, GNode request, file file) throws RemoteException, IOException;

	public User user(String login, String password, String scheme) throws RemoteException;
	public User create(String login, String scheme) throws RemoteException;

	public String[] domains() throws RemoteException;

	public boolean has(Message message) throws RemoteException;
	public boolean accept(Message message) throws RemoteException;
}
