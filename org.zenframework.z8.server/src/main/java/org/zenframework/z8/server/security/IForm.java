package org.zenframework.z8.server.security;

import java.io.Serializable;

public interface IForm extends Serializable {
    public String getId();

    public String getName();

    public String getDescription();

    public String getOwnerId();

    public String getOwnerName();

    public String getGroupId();

    public String getGroupName();

    public IAccess getAccess();
}
