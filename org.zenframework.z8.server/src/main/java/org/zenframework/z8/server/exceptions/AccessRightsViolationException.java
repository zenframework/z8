package org.zenframework.z8.server.exceptions;

import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.utils.ErrorUtils;

public class AccessRightsViolationException extends exception {
	private static final long serialVersionUID = 2127190490820439197L;

	private static String message = Resources.get("Exception.accessRightsViolation");
	
	public AccessRightsViolationException() {
		super(message);
	}

	public AccessRightsViolationException(String message) {
		super(message);
	}

	public AccessRightsViolationException(Throwable e) {
		super(message + "\n" + ErrorUtils.getMessage(e));
	}
}
