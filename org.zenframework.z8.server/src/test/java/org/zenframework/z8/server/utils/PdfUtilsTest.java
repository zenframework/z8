package org.zenframework.z8.server.utils;

import java.io.File;

import junit.framework.TestCase;

public class PdfUtilsTest extends TestCase {

	public PdfUtilsTest(String name) {
		super(name);
	}

	public void testIsTiff() throws Exception {
		File temp1 = File.createTempFile("z8-test-", ".tiff");
		File temp2 = File.createTempFile("z8-test-", ".xxx");
		try {
			IOUtils.copy(getClass().getClassLoader().getResourceAsStream("z8.tiff"), temp1);
			IOUtils.copy(getClass().getClassLoader().getResourceAsStream("z8.tiff"), temp2);
			assertTrue(PdfUtils.isTiff(temp1));
			assertTrue(PdfUtils.isTiff(temp2));
		} finally {
			temp1.delete();
			temp2.delete();
		}
	}

}
