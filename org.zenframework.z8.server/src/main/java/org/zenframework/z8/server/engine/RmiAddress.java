package org.zenframework.z8.server.engine;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.ie.TransportException;

public class RmiAddress {

	public final String host;
	public final int port;
	public final String id;

	public RmiAddress(String address) throws TransportException {
		if (address == null)
			throw new RuntimeException("Incorrect RMI address '" + address + "'");
		if (address.startsWith("rmi://"))
			address = address.substring(6);
		if (address.startsWith("rmi:"))
			address = address.substring(4);
		try {
			int hostAndPort = address.indexOf(':');
			int portAndId = address.indexOf('/');
			host = address.substring(0, hostAndPort < 0 ? (portAndId < 0 ? address.length() : portAndId) : hostAndPort);
			port = hostAndPort < 0 ? ServerConfig.RegistryPortDefault : Integer.parseInt(address.substring(hostAndPort + 1,
					portAndId < 0 ? address.length() : portAndId));
			id = portAndId < 0 ? null : address.substring(portAndId + 1);
		} catch (Throwable e) {
			throw new RuntimeException("Incorrect RMI address '" + address + "'", e);
		}
	}

	@Override
	public String toString() {
		return Rmi.url(host, port, id);
	}

}
