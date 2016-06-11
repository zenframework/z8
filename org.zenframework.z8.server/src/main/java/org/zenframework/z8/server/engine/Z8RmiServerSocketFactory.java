package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

public class Z8RmiServerSocketFactory implements RMIServerSocketFactory {

	@Override
	public ServerSocket createServerSocket(int port) throws IOException {
		if (port == 0)
			port = Z8Context.getConfig().getRmiServersPortRange().getRandomPort();
		return new ServerSocket(port);
	}

}
