package org.zenframework.z8.server.exceptions;

import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.exception;

public class UserNotFoundException extends exception {
	private static final long serialVersionUID = 4530160455494913676L;

	private static String message = Resources.get("Exception.userNotFound");

	public UserNotFoundException() {
		super(message);
	}
}
