package org.zenframework.z8.server.exceptions.db;

public final class DuplicateKeyFoundException extends RuntimeException {
    private static final long serialVersionUID = 4171626525720510361L;

    public DuplicateKeyFoundException(String reason, String SQLState, int vendorCode) {
        super(reason);
    }

}
