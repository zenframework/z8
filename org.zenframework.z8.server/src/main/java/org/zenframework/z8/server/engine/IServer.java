package org.zenframework.z8.server.engine;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServer extends Remote {

	public void start() throws RemoteException;

	public void stop() throws RemoteException;

	public String id() throws RemoteException;

	public String getUrl() throws RemoteException;

}
