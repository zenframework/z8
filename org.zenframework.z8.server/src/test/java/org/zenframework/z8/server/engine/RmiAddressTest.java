package org.zenframework.z8.server.engine;

import java.rmi.registry.Registry;

import org.zenframework.z8.server.ie.TransportException;

import junit.framework.TestCase;

public class RmiAddressTest extends TestCase {

	public RmiAddressTest(String name) {
		super(name);
	}

	public void testRmiAddress() throws Exception {
		assertRmiAddress("rmi://qweqwe:123#asd", "qweqwe", 123, "asd");
		assertRmiAddress("rmi:qweqwe:123#asd", "qweqwe", 123, "asd");
		assertRmiAddress("qweqwe:123#asd", "qweqwe", 123, "asd");
		assertRmiAddress("qweqwe:123", "qweqwe", 123, null);
		assertRmiAddress("qweqwe#asd", "qweqwe", Registry.REGISTRY_PORT, "asd");
		assertRmiAddress("qweqwe", "qweqwe", Registry.REGISTRY_PORT, null);
	}

	private static void assertRmiAddress(String address, String host, int port, String id) throws TransportException {
		RmiAddress rmiAddress = new RmiAddress(address);
		assertEquals(host, rmiAddress.host);
		assertEquals(port, rmiAddress.port);
		assertEquals(id, rmiAddress.id);
	}

}
