package org.zenframework.z8.compiler.cmd;

public class CompilerException extends Exception {

	private static final long serialVersionUID = -1946322059940654507L;

	public CompilerException(String message) {
		super(message);
	}

	public CompilerException(Throwable cause) {
		super(cause);
	}

	public CompilerException(String message, Throwable cause) {
		super(message, cause);
	}
}
