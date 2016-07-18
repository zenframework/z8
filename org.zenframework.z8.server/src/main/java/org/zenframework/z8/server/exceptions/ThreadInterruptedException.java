package org.zenframework.z8.server.exceptions;

import org.zenframework.z8.server.types.exception;

public final class ThreadInterruptedException extends exception {
    private static final long serialVersionUID = -1283069988614148258L;

    public ThreadInterruptedException() {
    	this(null);
    }
    
    public ThreadInterruptedException(String value) {
        super("Thread '" + Thread.currentThread().getName() + "' is interrupted");
    }
}
