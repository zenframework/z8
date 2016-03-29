package org.zenframework.z8.server.engine;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.utils.FileKeyValue;
import org.zenframework.z8.server.utils.IKeyValue;

public class TransportCenter extends RmiServer implements ITransportCenter {

	private static TransportCenter INSTANCE;

	private final IKeyValue<String, String> store = new FileKeyValue(new File(Z8Context.getConfig().getWorkingPath(),
			"transport-servers.xml"));

	protected TransportCenter() throws RemoteException {
		super(ITransportCenter.class);
	}

	private static final long serialVersionUID = 5894774801318237091L;

	@Override
	public void registerTransportServer(String address, int localRegistryPort) throws RemoteException {
		try {
			store.set(address, RemoteServer.getClientHost() + ':' + localRegistryPort);
		} catch (Exception e) {
			throw new RemoteException("Can't register transport server '" + address + "'", e);
		}
	}

	@Override
	public String getTransportServerAddress(String address) throws RemoteException {
		return store.get(address);
	}

	public static void start(ServerConfig config) throws RemoteException {
		if (INSTANCE == null) {
			INSTANCE = new TransportCenter();
			INSTANCE.start();
		}
	}

}
