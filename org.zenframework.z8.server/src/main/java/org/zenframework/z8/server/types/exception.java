package org.zenframework.z8.server.types;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class exception extends RuntimeException {

	private static final long serialVersionUID = -3195100358970500549L;

	public exception() {
		super();
	}

	public exception(String message) {
		super(message);
	}

	public exception(string message) {
		this(message.get());
	}

	public exception(String message, Throwable e) {
		super(message, e);
	}

	public exception(Throwable e) {
		super(e);
	}

	public string string() {
		return z8_getMessage();
	}

	public string z8_getMessage() {
		return new string(getMessage());
	}

	public string z8_getStackTrace() {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(buf);
		printStackTrace(out);
		return new string(buf.toByteArray(), encoding.UTF8);
	}

}
