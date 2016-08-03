package org.zenframework.z8.server.exceptions.db;

public final class ObjectNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 4063768789453181624L;
	
	public ObjectNotFoundException(String reason) {
		super(reason);
	}

	public ObjectNotFoundException(String reason, Throwable e) {
		super(reason);
	}

    public ObjectNotFoundException(String reason, String SQLState, int vendorCode) {
        super(reason);
    }
}
