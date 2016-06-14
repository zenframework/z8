package org.zenframework.z8.server.config;

import junit.framework.TestCase;

public class PortRangeTest extends TestCase {

	public PortRangeTest(String name) {
		super(name);
	}

	public void testIsInRange() throws Exception {
		PortRange portRange = PortRange.parsePortRange("123-234,345,456-567");
		assertTrue(portRange.isInRange(123));
		assertTrue(portRange.isInRange(200));
		assertTrue(portRange.isInRange(234));
		assertTrue(portRange.isInRange(345));
		assertTrue(portRange.isInRange(456));
		assertTrue(portRange.isInRange(500));
		assertTrue(portRange.isInRange(567));
		assertTrue(!portRange.isInRange(100));
		assertTrue(!portRange.isInRange(300));
		assertTrue(!portRange.isInRange(400));
		assertTrue(!portRange.isInRange(600));
	}

	public void testGetRandomPort() throws Exception {
		PortRange portRange = PortRange.parsePortRange("123-234,345,456-567");
		for (int i = 0; i < 100; i++) {
			assertTrue(portRange.isInRange(portRange.getRandomPort()));
		}
	}

}
