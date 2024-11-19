package org.zenframework.z8.server.exceptions;

import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.exception;

public class UserBannedException extends exception {
	private static final long serialVersionUID = 925703120991687706L;
	private static String message = Resources.get("Exception.userBannedException");

	public UserBannedException(String key) {
		super(!key.equals("Exception.userBannedException") ? Resources.format("Exception.userBannedException",
                key) : "Exception.userBannedException");
	}
}