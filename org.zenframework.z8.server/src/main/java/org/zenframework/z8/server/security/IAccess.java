package org.zenframework.z8.server.security;

import java.io.Serializable;

public interface IAccess extends Serializable {
    public boolean getRead();

    public boolean getWrite();

    public boolean getDelete();

    public boolean getImport();
}
