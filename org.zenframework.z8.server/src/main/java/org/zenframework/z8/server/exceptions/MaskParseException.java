package org.zenframework.z8.server.exceptions;

import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.string;

public final class MaskParseException extends exception {
    private static final long serialVersionUID = -1283069988614148258L;

    public MaskParseException(string val, String mask) {
        super(Resources.format("Exception.maskParse", val, mask));
    }
}
