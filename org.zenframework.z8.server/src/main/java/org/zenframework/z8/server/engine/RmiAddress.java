package org.zenframework.z8.server.engine;

import java.net.URISyntaxException;

import org.zenframework.z8.server.config.ServerConfig;

public class RmiAddress {

	private final String host;
	private final int port;
	private final String name;

	public RmiAddress(String address) throws URISyntaxException {
		if (address == null)
			throw new URISyntaxException(address, "Address is null");
		if (address.startsWith("rmi://"))
			address = address.substring(6);
		if (address.startsWith("rmi:"))
			address = address.substring(4);
		try {
			int hostAndPort = address.indexOf(':');
			int portAndName = address.indexOf('/');
			host = address.substring(0, hostAndPort < 0 ? (portAndName < 0 ? address.length() : portAndName) : hostAndPort);
			port = hostAndPort < 0 ? ServerConfig.DefaultRegistryPort : Integer.parseInt(address.substring(hostAndPort + 1,
					portAndName < 0 ? address.length() : portAndName));
			name = portAndName < 0 ? null : address.substring(portAndName + 1);
		} catch (Throwable e) {
			throw new URISyntaxException(address, e.getClass().getName() + ": " + e.getMessage());
		}
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return Rmi.url(host, port, name);
	}

}
