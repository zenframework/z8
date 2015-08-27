package org.zenframework.z8.server.runtime;

import java.util.List;

public interface IClass<TYPE extends IObject> {
    public Class<TYPE> getJavaClass();

    public void setJavaClass(Class<?> cls);

    public boolean hasInstance();

    public TYPE get();

    public TYPE newInstance();

    public String getAttribute(String key);

    public void setAttribute(String key, String value);

    public boolean hasAttribute(String key);

    public List<IClass<TYPE>> getReferences();

    public String classId();

    public String displayName();

    public String description();
}
