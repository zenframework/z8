package org.zenframework.z8.server.exceptions.db;

import org.zenframework.z8.server.resources.Resources;

public final class ChildRecordFoundException extends RuntimeException {
    private static final long serialVersionUID = -7012122451651150968L;

    public ChildRecordFoundException(String _SQLState, int _vendorCode, String _cmd) {
        super(Resources.get("Exception.childRecordFound"));
    }
}
