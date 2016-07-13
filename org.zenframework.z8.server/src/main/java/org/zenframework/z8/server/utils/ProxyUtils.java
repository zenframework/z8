package org.zenframework.z8.server.utils;

import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObjectInvocationHandler;

import sun.rmi.server.UnicastRef;
import sun.rmi.transport.LiveRef;
import sun.rmi.transport.tcp.TCPEndpoint;

@SuppressWarnings("restriction")
public class ProxyUtils {
	
	static public String getHost(Proxy proxy) {
		return getEndpoint(proxy).getHost();
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
