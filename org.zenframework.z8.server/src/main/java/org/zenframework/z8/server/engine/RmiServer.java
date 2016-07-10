package org.zenframework.z8.server.engine;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;

public abstract class RmiServer implements IServer, Remote {
	private final Class<? extends IServer> serverClass;

	protected RmiServer(int port, Class<? extends IServer> serverClass) throws RemoteException {
		this.serverClass = serverClass;
		exportObject(port);
	}

	private void exportObject(int port) throws RemoteException {
		while(true) {
			try {
				UnicastRemoteObject.exportObject(this, port);
				return;
			} catch(ExportException e) {
				port++;
			}
		}
	}
	
	public void check() {
	}
	
	@Override
	public void probe() throws RemoteException {
	}

	@Override
	public void start() throws RemoteException {
		Rmi.register(serverClass, this);
	}

	@Override
	public void stop() throws RemoteException {
		UnicastRemoteObject.unexportObject(this, true);
		Rmi.unregister(serverClass, this);
	}
}