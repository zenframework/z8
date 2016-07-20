package org.zenframework.z8.server.utils;

import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObjectInvocationHandler;

import org.zenframework.z8.server.engine.IServer;
import org.zenframework.z8.server.engine.RmiServer;

import sun.rmi.server.UnicastRef;
import sun.rmi.transport.LiveRef;
import sun.rmi.transport.tcp.TCPEndpoint;

public class ProxyUtils {
	static public Proxy getProxy(IServer server) {
		return server instanceof Proxy ? (Proxy)server : ((RmiServer)server).proxy();
	}
	
	static public String getUrl(IServer server) {
		return getUrl(getProxy(server));
	}

	static public String getUrl(Proxy proxy) {
		return getHost(proxy) + ":" + getPort(proxy);
	}

	static public String getHost(IServer server) {
		return getHost(getProxy(server));
	}

	static public String getHost(Proxy proxy) {
		return getEndpoint(proxy).getHost();
	}

	static public int getPort(IServer server) {
		return getPort(getProxy(server));
	}

	static public int getPort(Proxy proxy) {
		return getLiveRef(proxy).getPort();
	}

	static public LiveRef getLiveRef(Proxy proxy) {
		RemoteObjectInvocationHandler handler = (RemoteObjectInvocationHandler)Proxy.getInvocationHandler(proxy);
		UnicastRef unicastRef = (UnicastRef)handler.getRef();
		return unicastRef.getLiveRef();
	}

	static public TCPEndpoint getEndpoint(Proxy proxy) {
		try {
			return (TCPEndpoint)getLiveRef(proxy).getChannel().getEndpoint();
		} catch(RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
	static public Proxy newProxy(LiveRef liveRef, Class<?>[] interfaces) {
		RemoteObjectInvocationHandler handler = new RemoteObjectInvocationHandler(new UnicastRef(liveRef));
		return (Proxy)Proxy.newProxyInstance(ProxyUtils.class.getClassLoader(), interfaces, handler);
	}
}
