package org.zenframework.z8.server.security;

import java.io.Serializable;

public class Component implements Serializable {
    private static final long serialVersionUID = 3995824646818479932L;

    private String id;
    private String className;
    private String title;

    public Component(String id, String className, String title) {
        this.id = id;
        this.className = className;
        this.title = title;
    }

    public String id() {
        return id;
    }

    public String className() {
        return className;
    }

    public String title() {
        return title;
    }

    @Override
    public boolean equals(Object object) {
        if(object == null)
            return false;
        if(object == this)
            return true;
        if(this.getClass() != object.getClass())
            return false;

        Component component = (Component)object;
        if(this.hashCode() == component.hashCode())
            return true;

        return false;
    }

    @Override
    public int hashCode() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(this.className);
        return buffer.toString().hashCode();
    }

}
