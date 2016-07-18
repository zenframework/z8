package org.zenframework.z8.server.ie;

import junit.framework.TestCase;

public class ImportPolicyTest extends TestCase {

	public ImportPolicyTest(String name) {
		super(name);
	}

	public void testGetPlicy() throws Exception {
		Throwable t = null;
		try {
			assertEquals(ImportPolicy.DEFAULT, ImportPolicy.getPolicy("qwe"));
		} catch (IllegalArgumentException e) {
			t = e;
		}
		assertTrue(t instanceof IllegalArgumentException);
		assertEquals(ImportPolicy.DEFAULT, ImportPolicy.getPolicy(null));
		assertEquals(ImportPolicy.DEFAULT, ImportPolicy.getPolicy(""));
		assertEquals(ImportPolicy.AGGREGATE, ImportPolicy.getPolicy("AGGREGATE"));
	}

}
