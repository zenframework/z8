package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Property extends OBJECT {

    public static final String Key = IObject.Name;
    public static final String Description = IObject.Description;
    public static final String DefaultValue = "defaultValue";

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
        setAttribute(IObject.ObjectId, new guid(id).toString());
        setAttribute(Key, key);
        setAttribute(DefaultValue, defaultValue);
        setAttribute(Description, description);
    }

    public guid getId() {
        return new guid(getAttribute(IObject.ObjectId));
    }

    public String getKey() {
        return getAttribute(Key);
    }

    public boolean equalsKey(String key) {
        return getKey().equals(key);
    }

    public String getDefaultValue() {
        return getAttribute(DefaultValue);
    }

    public String getDescription() {
        return getAttribute(Description);
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Property.CLASS<? extends Property> z8_getProperty(RCollection data) {
        Property.CLASS<Property> property = new Property.CLASS<Property>();
        RCollection<primary> values = (RCollection<primary>) data;
        if (data.size() < 3 || data.size() > 4) {
            throw new exception("Incorrect property definition: " + data);
        }
        property.setAttribute(IObject.ObjectId, new guid(values.get(0).toString()).toString());
        property.setAttribute(Key, values.get(1).toString());
        property.setAttribute(DefaultValue, values.get(2).toString());
        if (data.size() > 3) {
            property.setAttribute(Description, data.get(3).toString());
        }
        return property;
    }

    public static Property.CLASS<? extends Property> z8_getProperty(guid id, string key, primary defaultValue, string description) {
        Property.CLASS<Property> property = new Property.CLASS<Property>();
        property.setAttribute(IObject.ObjectId, new guid(id).toString());
        property.setAttribute(Key, key.get());
        property.setAttribute(DefaultValue, defaultValue.toString());
        property.setAttribute(Description, description.get());
        return property;
    }

    public string z8_key() {
        return new string(getKey());
    }

    public string z8_description() {
        return new string(getDescription());
    }

    public string z8_defaultValue() {
        return new string(getDefaultValue());
    }

}
