package org.zenframework.z8.server.engine;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public abstract class RmiServer extends UnicastRemoteObject implements IServer {

	private static final long serialVersionUID = -1200219220297838398L;

	private transient final Class<? extends IServer> serverClass;
	private transient String url;

	protected RmiServer(int unicastPort, Class<? extends IServer> serverClass) throws RemoteException {
		super(unicastPort);
		this.serverClass = serverClass;
	}

	@Override
	public String id() throws RemoteException {
		return null;
	}

	@Override
	public String getUrl() throws RemoteException {
		return url;
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