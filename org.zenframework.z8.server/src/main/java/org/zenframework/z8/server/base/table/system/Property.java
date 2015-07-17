package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class Property extends OBJECT {

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

    private guid id;
    private string key;
    private string defaultValue;
    private string description = new string("");

    public Property(IObject container) {
        super(container);
    }
    
    public Property(String id, String key, String defaultValue, String description) {
        this.id = new guid(id);
        this.key = new string(key);
        this.defaultValue = new string(defaultValue);
        this.description = new string(description);
    }

    public guid getId() {
        return id;
    }

    public String getKey() {
        return key.get();
    }
    
    public boolean equalsKey(String key) {
        return this.key.equals(key);
    }

    public String getDefaultValue() {
        return defaultValue.get();
    }

    public String getDescription() {
        return description.get();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Property && id.equals(((Property) obj).id);
    }

    @Override
    public String toString() {
        return new StringBuilder(1024).append("Property[").append(id).append(", ").append(key).append(", ")
                .append(defaultValue).append(", ").append(description).append(']').toString();
    }

    public void operatorAssign(RCollection<string> data) {
        if (data.size() < 3 || data.size() > 4) {
            throw new exception("Incorrect property definition: " + data);
        }
        id = new guid(data.get(0).get());
        key = data.get(1);
        defaultValue = data.get(2);
        if (data.size() > 3) {
            description = data.get(3);
        }
    }

}
