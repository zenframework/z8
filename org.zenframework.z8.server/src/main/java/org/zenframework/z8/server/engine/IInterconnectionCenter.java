package org.zenframework.z8.server.engine;

import java.rmi.RemoteException;

import org.zenframework.z8.server.types.file;

public interface IInterconnectionCenter extends IHubServer {
	IApplicationServer connect(String domain) throws RemoteException;
	
	public boolean accept(IApplicationServer server, Object data) throws RemoteException;
	public boolean hasFile(IApplicationServer server, file file) throws RemoteException;
}
