package org.zenframework.z8.server.exceptions.db;

import org.zenframework.z8.server.resources.Resources;

public final class UnknownDatabaseException extends RuntimeException {
    private static final long serialVersionUID = -6219227818864157009L;

    public UnknownDatabaseException() {
        super(Resources.get("Exception.unknownDatabase"));
    }

    public UnknownDatabaseException(String message, Throwable e) {
        super(message, e);
    }
}
