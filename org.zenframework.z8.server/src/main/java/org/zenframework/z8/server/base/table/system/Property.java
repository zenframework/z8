package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class Property extends OBJECT {

    private static final String DEFAULT_VALUE = "defaultValue";

    public static class CLASS<T extends Property> extends OBJECT.CLASS<T> {

        public CLASS() {
            this(null);
        }

        public CLASS(IObject container) {
            super(container);
            setJavaClass(Property.class);
            setName(Property.class.getName());
            setDisplayName(Property.class.getName());
        }

        @Override
        public Object newObject(IObject container) {
            return new Property(container);
        }

    }

    public Property(IObject container) {
        super(container);
    }
    
    public Property(String id, String key, String defaultValue, String description) {
        setAttribute(IObject.Guid, new guid(id).toString());
        setAttribute(IObject.Name, key);
        setAttribute(DEFAULT_VALUE, defaultValue);
        setAttribute(IObject.Description, description);
    }

    public guid getId() {
        return new guid(getAttribute(IObject.Guid));
    }

    public String getKey() {
        return getAttribute(IObject.Name);
    }
    
    public boolean equalsKey(String key) {
        return getKey().equals(key);
    }

    public String getDefaultValue() {
        return getAttribute(DEFAULT_VALUE);
    }

    public String getDescription() {
        return getAttribute(IObject.Description);
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Property && getId().equals(((Property) obj).getId());
    }

    @Override
    public String toString() {
        return new StringBuilder(1024).append("Property[").append(getId()).append(", ").append(getKey()).append(", ")
                .append(getDefaultValue()).append(", ").append(getDescription()).append(']').toString();
    }

    public void operatorAssign(RCollection<string> data) {
        if (data.size() < 3 || data.size() > 4) {
            throw new exception("Incorrect property definition: " + data);
        }
        setAttribute(IObject.Guid, new guid(data.get(0).get()).toString());
        setAttribute(IObject.Name, data.get(1).get());
        setAttribute(DEFAULT_VALUE, data.get(2).get());
        if (data.size() > 3) {
            setAttribute(IObject.Description, data.get(3).get());
        }
    }

    public guid z8_getId() {
        return getId();
    }

    public string z8_getKey() {
        return new string(getKey());
    }

    public string z8_getDescription() {
        return new string(getDescription());
    }

    public string z8_getDefaultValue() {
        return new string(getDefaultValue());
    }

}
