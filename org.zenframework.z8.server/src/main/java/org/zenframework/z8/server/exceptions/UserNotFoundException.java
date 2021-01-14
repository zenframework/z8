package org.zenframework.z8.server.exceptions;

import org.zenframework.z8.server.types.exception;

public class UserNotFoundException extends exception {

	public UserNotFoundException() {
		super("User not found");
	}
}
