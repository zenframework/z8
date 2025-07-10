package org.zenframework.z8.justintime.runtime;

public class JustInTimeException extends Exception {

	private static final long serialVersionUID = 1L;

	public JustInTimeException(String message) {
		super(message);
	}

	public JustInTimeException(Throwable cause) {
		super(cause);
	}

	public JustInTimeException(String message, Throwable cause) {
		super(message, cause);
	}

}
