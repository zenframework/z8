package org.zenframework.z8.server.engine;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.logs.Trace;

public class Rmi {

	static public String localhost = "localhost";
	static public int defaultRegistryPort = 7852;
	
	static private Map<String, IServer> servers = new HashMap<String, IServer>();

	static private Registry registry;

	static public void register(IServer server) throws RemoteException {
		String name = server.name();

		servers.put(name, server);

		if(ServerConfig.rmiEnabled())
			rebind(name, server);
	}

	static public void unregister(IServer server) throws RemoteException {
		String name = server.name();

		servers.remove(server);

		if(ServerConfig.rmiEnabled())
			unbind(name);
	}

	static public IServer get(String name) throws RemoteException {
		return get(name, Rmi.localhost, 0);
	}

	@SuppressWarnings("unchecked")
	static public <TYPE> TYPE get(Class<TYPE> cls, String host, int port) throws RemoteException {
		return (TYPE)get(RmiServer.serverName(cls), host, port);
	}
	
	static public IServer get(String name, String host, int port) throws RemoteException {
		IServer server = (IServer)servers.get(name);

		if(server != null)
			return server;

		try {
			return (IServer)LocateRegistry.getRegistry(host, ServerConfig.rmiRegistryPort()).lookup(name);
		} catch(NotBoundException e) {
			throw new RemoteException("Object '" + name + "' is not bound", e);
		}
	}

	static private Registry getRegistry() {
		if(registry != null)
			return registry;

		int port = ServerConfig.rmiRegistryPort();

		try {
			return registry = LocateRegistry.createRegistry(port);
		} catch(RemoteException e) {
		}

		try {
			return registry = LocateRegistry.getRegistry(port);
		} catch(Throwable e1) {
			throw new RuntimeException(e1);
		}
	}

	static private void rebind(String name, IServer server) throws RemoteException {
		getRegistry().rebind(name, server);
	}

	static private void unbind(String name) throws RemoteException {
		try {
			getRegistry().unbind(name);
		} catch(NotBoundException e) {
			Trace.logError("Can't unbind object '" + name + "'", e);
		}
	}
}
