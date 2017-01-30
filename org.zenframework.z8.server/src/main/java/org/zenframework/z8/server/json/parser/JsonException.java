package org.zenframework.z8.server.json.parser;

/**
 * The JSONException is thrown by the JSON.org classes when things are amiss.
 * 
 * @author JSON.org
 * @version 2010-12-24
 */
public class JsonException extends RuntimeException {
	private static final long serialVersionUID = 0;
	private Throwable cause;

	/**
	 * Constructs a JSONException with an explanatory message.
	 * 
	 * @param message
	 *            Detail about the reason for the exception.
	 */
	public JsonException(String message) {
		super(message);
	}

	public JsonException(Throwable cause) {
		super(cause.getMessage());
		this.cause = cause;
	}

	@Override
	public Throwable getCause() {
		return this.cause;
	}
}
