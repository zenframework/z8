package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.rmi.RemoteException;

import org.zenframework.z8.server.base.xml.GNode;
import org.zenframework.z8.server.ie.BaseMessage;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.types.file;

public interface IApplicationServer extends IServer {
	public GNode processRequest(ISession session, GNode request) throws RemoteException;

	public file download(file file) throws RemoteException, IOException;

	public IUser login(String login) throws RemoteException;
	public IUser login(String login, String password) throws RemoteException;

	public String[] domains() throws RemoteException;

	public boolean has(BaseMessage message) throws RemoteException;
	public boolean accept(BaseMessage message) throws RemoteException;
}
