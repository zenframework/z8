package org.zenframework.z8.server.exceptions;

import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.exception;

public class InvalidVersionException extends exception {
	private static final long serialVersionUID = -3158089161055849459L;

	public static String Message = Resources.get("Exception.invalidVersion");

	public InvalidVersionException() {
		super(Message);
	}
}
