package org.zenframework.z8.compiler.workspace;

public interface ResourceListener {
    public final static int RESOURCE_ADDED = 0;
    public final static int RESOURCE_REMOVED = 1;
    public final static int RESOURCE_CHANGED = 2;
    public final static int BUILD_COMPLETE = 3;

    void event(int type, Resource resource, Object object);
}
