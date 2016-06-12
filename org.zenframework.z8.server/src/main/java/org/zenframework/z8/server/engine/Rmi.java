package org.zenframework.z8.server.engine;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.server.config.ServerConfig;

public class Rmi {

	private static final Log LOG = LogFactory.getLog(Rmi.class);

	private static final Map<String, IServer> Servers = new HashMap<String, IServer>();

	private static String Host;
	private static int Port;
	private static Registry Registry;

	public static void init(ServerConfig config) throws RemoteException, UnknownHostException {
		Port = config.getRmiRegistryPort();
		Host = InetAddress.getLocalHost().getHostAddress();

		LOG.info("RMI enabled: " + (Port >= 0));

		if (Port >= 0) {
			try {
				Registry = LocateRegistry.createRegistry(Port);
				LOG.info("RMI registry created at " + Host + ':' + Port);
			} catch (RemoteException e) {
				Registry = LocateRegistry.getRegistry(Port);
				LOG.info("RMI registry located at " + Host + ':' + Port);
			}
		}
	}

	public static String url(String host, int port, String name) {
		return "rmi://" + host + ':' + port + '/' + (name != null ? name : "");
	}

	public static String getName(Class<?> cls) {
		return cls.getSimpleName();
	}

	public static int getPort() {
		return Port;
	}

	public static String register(Class<? extends IServer> serverClass, IServer server) throws RemoteException {
		String name = getName(serverClass);
		Servers.put(url(Host, Port, name), server);
		if (server.id() != null)
			name += '/' + server.id();
		if (Registry != null)
			Registry.rebind(name, server);
		return url(Host, Port, name);
	}

	public static void unregister(Class<? extends IServer> serverClass, IServer server) throws RemoteException {
		String name = getName(serverClass);
		Servers.remove(url(Host, Port, name));
		if (server.id() != null)
			name += '/' + server.id();
		if (Registry != null) {
			try {
				Registry.unbind(name);
			} catch (NotBoundException e) {
				LOG.error("Can't unbind object '" + name + "'", e);
			}
		}
	}

	public static <T extends IServer> T get(Class<T> serverClass) throws RemoteException {
		return get(serverClass, Host, Port);
	}

	public static IServer get(String name) throws RemoteException {
		return get(name, Host, Port);
	}

	@SuppressWarnings("unchecked")
	public static <T extends IServer> T get(Class<T> serverClass, String host, int port) throws RemoteException {
		return (T) get(getName(serverClass), host, port);
	}

	public static IServer get(String name, String host, int port) throws RemoteException {
		if (host == null || host.isEmpty())
			host = Host;
		try {
			if (Host.equals(host)) {
				if (Port == port) {
					IServer server = (IServer) Servers.get(url(host, port, name));
					if (server != null)
						return server;
				}
				return (IServer) Registry.lookup(name);
			} else {
				return (IServer) LocateRegistry.getRegistry(host, port).lookup(name);
			}
		} catch (NotBoundException e) {
			throw new RemoteException("Object " + name + " is not bound", e);
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

	public static String getClientHost() {
		try {
			// Try detect remote client host
			return RemoteServer.getClientHost();
		} catch (ServerNotActiveException e) {
			// If ServerNotActiveException, server object was called locally
			return null;
		}
	}

}
