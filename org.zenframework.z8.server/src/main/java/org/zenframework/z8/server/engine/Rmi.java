package org.zenframework.z8.server.engine;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.utils.ErrorUtils;

public class Rmi {

	private static final Log LOG = LogFactory.getLog(Rmi.class);

	private static final Map<String, IServer> servers = new HashMap<String, IServer>();
	private static String HOST;
	private static int PORT;
	private static Registry REGISTRY;

	public static void init(ServerConfig config) throws RemoteException, UnknownHostException {
		PORT = config.getAuthorityCenterPort();
		if (config.getAuthorityCenterHost().isEmpty()) {
			HOST = InetAddress.getLocalHost().getHostAddress();
			try {
				REGISTRY = LocateRegistry.createRegistry(PORT);
				LOG.trace("RMI registry created at port " + PORT);
			} catch (RemoteException e) {
				REGISTRY = LocateRegistry.getRegistry(PORT);
				LOG.trace("RMI registry located at port " + PORT);
			}
		} else {
			HOST = config.getAuthorityCenterHost();
			REGISTRY = LocateRegistry.getRegistry(HOST, PORT);
			LOG.trace("RMI registry located at port " + PORT);
		}
	}

	public static String url(String host, int port, String name) {
		return "rmi://" + host + ":" + port + "/" + name;
	}

	public static int getPort() {
		return PORT;
	}

	public static String register(Class<? extends IServer> serverClass, IServer server) throws RemoteException {
		String name = serverClass.getSimpleName();
		servers.put(url(HOST, PORT, name), server);
		if (server.id() != null)
			name += '/' + server.id();
		REGISTRY.rebind(name, server);
		return url(HOST, PORT, name);
	}

	public static void unregister(Class<? extends IServer> serverClass, IServer server) throws RemoteException {
		String name = serverClass.getSimpleName();
		servers.remove(url(HOST, PORT, name));
		if (server.id() != null)
			name += '/' + server.id();
		try {
			REGISTRY.unbind(name);
		} catch (NotBoundException e) {}
	}

	public static <T extends IServer> T get(Class<T> serverClass) throws RemoteException {
		return get(serverClass, HOST, PORT);
	}

	@SuppressWarnings("unchecked")
	public static <T extends IServer> T get(Class<T> serverClass, String host, int port) throws RemoteException {
		if (host == null || host.isEmpty())
			host = HOST;
		if (HOST.equals(host) && PORT == port) {
			T server = (T) servers.get(url(host, port, serverClass.getSimpleName()));
			if (server != null)
				return server;
		}
		try {
			return (T) LocateRegistry.getRegistry(host, port).lookup(serverClass.getSimpleName());
		} catch (Throwable e) {
			throw new RemoteException(ErrorUtils.getMessage(e), e);
		}
	}

}
