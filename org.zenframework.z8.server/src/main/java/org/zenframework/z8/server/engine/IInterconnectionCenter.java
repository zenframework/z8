package org.zenframework.z8.server.engine;

import java.rmi.RemoteException;

import org.zenframework.z8.server.ie.BaseMessage;

public interface IInterconnectionCenter extends IHubServer {
	IApplicationServer connect(String domain) throws RemoteException;
	
	public boolean has(IApplicationServer server, BaseMessage message) throws RemoteException;
	public boolean accept(IApplicationServer server, BaseMessage message) throws RemoteException;
}
