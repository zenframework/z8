package org.zenframework.z8.server.engine;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.logs.Trace;

public class Rmi {

	private static final Map<String, IServer> Servers = new HashMap<String, IServer>();

	private static String localHost;
	private static int registryPort;
	private static Registry Registry;

	public static void init(ServerConfig config) throws RemoteException, UnknownHostException {
		registryPort = config.getRmiRegistryPort();
		localHost = InetAddress.getLocalHost().getHostAddress();

		try {
			Registry = LocateRegistry.createRegistry(registryPort);
		} catch (RemoteException e) {
			Registry = LocateRegistry.getRegistry(registryPort);
		}
	}

	public static String url(String host, int port, String name) {
		if(host == null || host.isEmpty())
			host = localHost;
		
		return "rmi://" + host + ':' + port + '/' + name;
	}

	public static String getName(Class<?> cls) {
		return cls.getSimpleName();
	}

	public static int getPort() {
		return registryPort;
	}

	public static String register(Class<? extends IServer> serverClass, IServer server) throws RemoteException {
		String name = getName(serverClass);
		Servers.put(url(localHost, registryPort, name), server);
		Registry.rebind(name, server);
		return url(localHost, registryPort, name);
	}

	public static void unregister(Class<? extends IServer> serverClass, IServer server) throws RemoteException {
		String name = getName(serverClass);
		Servers.remove(url(localHost, registryPort, name));
		try {
			Registry.unbind(name);
		} catch (NotBoundException e) {
			Trace.logError("Can't unbind object '" + name + "'", e);
		}
	}

	public static <T extends IServer> T get(Class<T> serverClass) throws RemoteException {
		return get(serverClass, localHost, registryPort);
	}

	public static IServer get(String name) throws RemoteException {
		return get(name, localHost, registryPort);
	}

	@SuppressWarnings("unchecked")
	public static <T extends IServer> T get(Class<T> serverClass, String host, int port) throws RemoteException {
		return (T) get(getName(serverClass), host, port);
	}

	public static IServer get(String name, String host, int port) throws RemoteException {
		try {
			if (host == null || host.isEmpty() || localHost.equals(host)) {
				if (registryPort == port) {
					IServer server = (IServer) Servers.get(url(host, port, name));
					if (server != null)
						return server;
				}
				return (IServer) Registry.lookup(name);
			} else {
				return (IServer) LocateRegistry.getRegistry(host, registryPort).lookup(name);
			}
		} catch (NotBoundException e) {
			throw new RemoteException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends IServer> T get(Class<T> serverClass, RmiAddress rmiAddress) throws RemoteException {
		try {
			return (T) LocateRegistry.getRegistry(rmiAddress.getHost(), rmiAddress.getPort()).lookup(getName(serverClass));
		} catch (NotBoundException e) {
			throw new RemoteException("Object '" + rmiAddress.getName() + "' is not bound", e);
		}
	}

	public static IServer get(RmiAddress rmiAddress) throws RemoteException {
		try {
			return (IServer) LocateRegistry.getRegistry(rmiAddress.getHost(), rmiAddress.getPort()).lookup(
					rmiAddress.getName());
		} catch (NotBoundException e) {
			throw new RemoteException("Object '" + rmiAddress.getName() + "' is not bound", e);
		}
	}
}
