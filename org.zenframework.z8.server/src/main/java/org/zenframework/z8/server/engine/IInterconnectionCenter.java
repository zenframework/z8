package org.zenframework.z8.server.engine;

import java.rmi.RemoteException;

import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.types.string;

public interface IInterconnectionCenter extends IHubServer {
	IApplicationServer connect(String domain) throws RemoteException;
	IApplicationServer connect(string domain) throws RemoteException;

	public boolean has(IApplicationServer server, Message message) throws RemoteException;
	public boolean accept(IApplicationServer server, Message message) throws RemoteException;
}
