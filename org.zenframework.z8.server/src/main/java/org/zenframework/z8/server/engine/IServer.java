package org.zenframework.z8.server.engine;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServer extends Remote {
	String id() throws RemoteException;

	void start() throws RemoteException;
	void stop() throws RemoteException;

	void probe() throws RemoteException;
}
