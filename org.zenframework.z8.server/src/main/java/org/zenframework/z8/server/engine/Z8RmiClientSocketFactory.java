package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;

public class Z8RmiClientSocketFactory implements RMIClientSocketFactory {

	@Override
	public Socket createSocket(String host, int port) throws IOException {
		return new Socket(host, port);
	}

}
