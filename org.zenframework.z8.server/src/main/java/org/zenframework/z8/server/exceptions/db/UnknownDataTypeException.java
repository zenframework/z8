package org.zenframework.z8.server.exceptions.db;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.resources.Resources;

public final class UnknownDataTypeException extends RuntimeException {
    private static final long serialVersionUID = 5648419617588901507L;

    public UnknownDataTypeException(FieldType type) {
        super(Resources.format("Exception.unknownDataType", type.toString()));
    }

    public UnknownDataTypeException(int type) {
        super(Resources.format("Exception.unknownDataType", Integer.toString(type)));
    }
}
