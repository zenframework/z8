package org.zenframework.z8.server.engine;

import java.lang.reflect.Proxy;
import java.rmi.server.ObjID;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.utils.ProxyUtils;

import sun.rmi.transport.LiveRef;
import sun.rmi.transport.tcp.TCPEndpoint;

public class Rmi {

	static public String localhost = TCPEndpoint.getLocalEndpoint(0).getHost();

	static private Map<Class<?>, IServer> servers = new HashMap<Class<?>, IServer>();

	static public void register(IServer server) {
		servers.put(serverClass(server.getClass()), server);
	}

	static public void unregister(IServer server) {
		servers.remove(serverClass(server.getClass()));
	}

	static public <TYPE> TYPE get(Class<TYPE> cls) {
		return get(cls, Rmi.localhost, 0);
	}

	@SuppressWarnings({ "unchecked" })
	static public <TYPE> TYPE get(Class<TYPE> cls, String host, int port) {
		IServer server = servers.get(serverClass(cls));

		if(server != null)
			return (TYPE)server;

		Class<?>[] interfaces = { cls, IServer.class };
		LiveRef liveRef = new LiveRef(new ObjID(), new TCPEndpoint(host, port), false);

		return (TYPE)ProxyUtils.newProxy(liveRef, interfaces);
	}

	@SuppressWarnings("rawtypes")
	static public Proxy getProxy(Class cls, String host, int port) {
		Class<?>[] interfaces = { serverClass(cls), IServer.class };
		LiveRef liveRef = new LiveRef(new ObjID(), new TCPEndpoint(host, port), false);
		return ProxyUtils.newProxy(liveRef, interfaces);
	}

	static public Class<?> serverClass(Class<?> cls) {
		Class<?> subinterface = IServer.class;

		if(cls.isInterface() && subinterface.isAssignableFrom(cls))
			return cls;

		Class<?>[] interfaces = cls.getInterfaces();

		for(Class<?> i : interfaces) {
			if(subinterface.isAssignableFrom(i))
				subinterface = i;
		}

		if(subinterface == IServer.class)
			throw new RuntimeException("Class '" + cls.getCanonicalName() + "' does not implement subinterface of '" + IServer.class.getCanonicalName() + "'");

		return subinterface;
	}
}
