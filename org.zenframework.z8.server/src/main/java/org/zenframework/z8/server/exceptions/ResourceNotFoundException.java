package org.zenframework.z8.server.exceptions;

import org.zenframework.z8.server.resources.Resources;

public final class ResourceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = -8697055300394303266L;

    public ResourceNotFoundException(String key) {
        super(!key.equals("Exception.resourceNotFound") ? Resources.format("Exception.resourceNotFound",
                key) : "Exception.resourceNotFound");
    }
}
