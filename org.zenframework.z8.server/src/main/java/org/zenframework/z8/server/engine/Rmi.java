package org.zenframework.z8.server.engine;

import java.net.InetAddress;
import java.net.URI;
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
import org.zenframework.z8.server.exceptions.AccessDeniedException;

public class Rmi {

	private static final Log LOG = LogFactory.getLog(Rmi.class);

	private static final Map<String, IServer> Servers = new HashMap<String, IServer>();
	private static final Object Lock = new Object();

	private static ServerConfig Config;
	private static String Host;
	private static int Port;
	private static Registry Registry;

	private static IAuthorityCenter AuthorityCenter;

	public static void init(ServerConfig config) throws RemoteException, UnknownHostException {
		if (Config != null)
			return;

		Config = config;

		Port = config.getRmiRegistryPort();
		Host = InetAddress.getLocalHost().getHostAddress();

		try {
			Registry = LocateRegistry.createRegistry(Port);
			LOG.trace("RMI registry created at " + Host + ':' + Port);
		} catch (RemoteException e) {
			Registry = LocateRegistry.getRegistry(Port);
			LOG.trace("RMI registry located at " + Host + ':' + Port);
		}

	}

	public static ServerConfig getConfig() {
		return Config;
	}

	public static IAuthorityCenter getAuthorityCenter() {
		if (AuthorityCenter != null)
			return AuthorityCenter;

		synchronized (Lock) {
			if (AuthorityCenter != null)
				return AuthorityCenter;

			try {
				AuthorityCenter = get(IAuthorityCenter.class, Config.getAuthorityCenterHost(),
						Config.getAuthorityCenterPort());
				return AuthorityCenter;
			} catch (Throwable e) {
				throw new AccessDeniedException();
			}
		}
	}

	public static String url(String host, int port, String name) {
		return "rmi://" + host + ":" + port + "/" + name;
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
		Registry.rebind(name, server);
		return url(Host, Port, name);
	}

	public static void unregister(Class<? extends IServer> serverClass, IServer server) throws RemoteException {
		String name = getName(serverClass);
		Servers.remove(url(Host, Port, name));
		if (server.id() != null)
			name += '/' + server.id();
		try {
			Registry.unbind(name);
		} catch (NotBoundException e) {}
	}

	public static <T extends IServer> T get(Class<T> serverClass) throws RemoteException {
		return get(serverClass, Host, Port);
	}

	@SuppressWarnings("unchecked")
	public static <T extends IServer> T get(Class<T> serverClass, String host, int port) throws RemoteException {
		if (host == null || host.isEmpty())
			host = Host;
		try {
			if (Host.equals(host)) {
				if (Port == port) {
					T server = (T) Servers.get(url(host, port, getName(serverClass)));
					if (server != null)
						return server;
				}
				return (T) Registry.lookup(getName(serverClass));
			} else {
				return (T) LocateRegistry.getRegistry(host, port).lookup(getName(serverClass));
			}
		} catch (NotBoundException e) {
			throw new RemoteException("Object " + getName(serverClass) + " is not bound", e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends IServer> T get(Class<T> serverClass, URI uri) throws RemoteException {
		try {
			return (T) LocateRegistry.getRegistry(uri.getHost(), uri.getPort()).lookup(uri.getPath());
		} catch (NotBoundException e) {
			throw new RemoteException("Object " + getName(serverClass) + " is not bound", e);
		}
	}

}
