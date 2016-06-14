package org.zenframework.z8.server.ie;

public class ImportException extends Exception {

	private static final long serialVersionUID = -8358080684149010654L;

	public ImportException(String message) {
		super(message);
	}

	public ImportException(Throwable cause) {
		super(cause);
	}

	public ImportException(String message, Throwable cause) {
		super(message, cause);
	}

}
