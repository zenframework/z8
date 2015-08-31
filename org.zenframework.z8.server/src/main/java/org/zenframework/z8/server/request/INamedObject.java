package org.zenframework.z8.server.request;

public interface INamedObject extends Comparable<INamedObject> {
    public String id();

    public String displayName();
}
