package org.zenframework.z8.server.engine;

import java.net.URISyntaxException;

import junit.framework.TestCase;

import org.zenframework.z8.server.config.ServerConfig;

public class RmiAddressTest extends TestCase {

	public RmiAddressTest(String name) {
		super(name);
	}

	public void testRmiAddress() throws Exception {
		assertRmiAddress("rmi://qweqwe:123/asd", "qweqwe", 123, "asd");
		assertRmiAddress("rmi:qweqwe:123/asd", "qweqwe", 123, "asd");
		assertRmiAddress("qweqwe:123/asd", "qweqwe", 123, "asd");
		assertRmiAddress("qweqwe:123", "qweqwe", 123, null);
		assertRmiAddress("qweqwe/asd", "qweqwe", ServerConfig.RegistryPortDefault, "asd");
		assertRmiAddress("qweqwe", "qweqwe", ServerConfig.RegistryPortDefault, null);
	}

	private static void assertRmiAddress(String address, String host, int port, String name) throws URISyntaxException {
		RmiAddress rmiAddress = new RmiAddress(address);
		assertEquals(host, rmiAddress.getHost());
		assertEquals(port, rmiAddress.getPort());
		assertEquals(name, rmiAddress.getName());
	}

}
