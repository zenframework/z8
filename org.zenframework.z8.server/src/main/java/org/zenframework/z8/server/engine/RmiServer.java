package org.zenframework.z8.server.engine;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public abstract class RmiServer extends UnicastRemoteObject implements IServer {

	private static final long serialVersionUID = -1200219220297838398L;

	private final Class<? extends IServer> serverClass;
	private final String hostOS;
	private String url;

	protected RmiServer(Class<? extends IServer> serverClass) throws RemoteException {
		this.serverClass = serverClass;
		this.hostOS = System.getProperty("os.name");
	}

	@Override
	public String id() throws RemoteException {
		return null;
	}

	@Override
	public String getUrl() throws RemoteException {
		return url;
	}

	public String hostOS() {
		return hostOS;
	}

	@Override
	public void start() throws RemoteException {
		url = Rmi.register(serverClass, this);
	}

	@Override
	public void stop() throws RemoteException {
		unexportObject(this, true);
		Rmi.unregister(serverClass, this);
		url = null;
	}

}
