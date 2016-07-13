package org.zenframework.z8.server.engine;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServer extends Remote {
	public String id() throws RemoteException;
	
	public void start() throws RemoteException;
	public void stop() throws RemoteException;

	public void probe() throws RemoteException;
}
