package org.zenframework.z8.server.engine;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public abstract class RmiServer implements IServer {

	private transient final int unicastPort;
	private transient final Class<? extends IServer> serverClass;
	private transient String url;

	protected RmiServer(int unicastPort, Class<? extends IServer> serverClass) throws RemoteException {
		this.unicastPort = unicastPort;
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
		if (unicastPort >= 0)
			UnicastRemoteObject.exportObject(this, unicastPort);
		url = Rmi.register(serverClass, this);
	}

	@Override
	public void stop() throws RemoteException {
		if (unicastPort >= 0)
			UnicastRemoteObject.unexportObject(this, true);
		Rmi.unregister(serverClass, this);
		url = null;
	}

}