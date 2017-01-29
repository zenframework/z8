package org.zenframework.z8.pde.build;

import java.io.PrintWriter;

public class JavaBuilderException extends Exception {
	private static final long serialVersionUID = -551138227179108371L;

	private String m_javaErrors;

	public JavaBuilderException(String javaErrors) {
		m_javaErrors = javaErrors;
	}

	@Override
	public void printStackTrace(PrintWriter printWriter) {
		printWriter.println(m_javaErrors);
	}

}
