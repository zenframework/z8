package org.zenframework.z8.server.engine;

import org.zenframework.z8.server.ie.TransportException;

public class RmiAddress {

	public final String host;
	public final int port;
	public final String id;

	public RmiAddress(String address) throws TransportException {
		if (address.startsWith("rmi://"))
			address = address.substring(6);
		try {
			int hostAndPort = address.indexOf(':');
			int portAndId = address.indexOf('/');
			host = address.substring(0, hostAndPort);
			port = Integer.parseInt(address.substring(hostAndPort + 1, portAndId));
			id = address.substring(portAndId + 1);
		} catch (Throwable e) {
			throw new TransportException("Can't parse RMI address '" + address + "'", e);
		}
	}

	@Override
	public String toString() {
		return Rmi.url(host, port, id);
	}

}
