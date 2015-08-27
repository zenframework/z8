package org.zenframework.z8.server.exceptions;

import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.utils.ErrorUtils;

public class AccessDeniedException extends exception {
    private static final long serialVersionUID = 2127190490820439197L;

    public AccessDeniedException() {
        super(Resources.get("Exception.accessDenied"));
    }

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(Throwable e) {
        super(Resources.get("Exception.accessDenied") + "\n" + ErrorUtils.getMessage(e));
    }
}
