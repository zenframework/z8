package org.zenframework.z8.server.exceptions.db;

public final class ObjectAlreadyExistException extends RuntimeException {
    private static final long serialVersionUID = -7539772335347059271L;

    public ObjectAlreadyExistException(String reason, String SQLState, int vendorCode) {
        super(reason);
    }

}
