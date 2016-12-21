package org.zenframework.z8.server.exceptions;

import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.utils.ErrorUtils;

public class AccessRightsViolationException extends exception {
	private static final long serialVersionUID = 2127190490820439197L;

	private static String Message = Resources.get("Exception.accessRightsViolation");

	private String message;

	public AccessRightsViolationException() {
		this(Message);
	}

	public AccessRightsViolationException(String message) {
		this.message = message;
	}

	public AccessRightsViolationException(Throwable e) {
		this(Message + "\n" + ErrorUtils.getMessage(e));
	}

	@Override
	public String toString() {
		return message;
	}
}
