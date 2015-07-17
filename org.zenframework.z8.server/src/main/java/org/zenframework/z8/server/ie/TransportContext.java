package org.zenframework.z8.server.ie;

import java.io.Serializable;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.string;

public class TransportContext extends OBJECT implements Serializable {

    private static final long serialVersionUID = 3103056587172568570L;

    public static class CLASS<T extends TransportContext> extends OBJECT.CLASS<T> {
        public CLASS() {
            this(null);
        }

        public CLASS(IObject container) {
            super(container);
            setJavaClass(TransportContext.class);
            setAttribute(Native, TransportContext.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new TransportContext(container);
        }

    }

    private final RLinkedHashMap<string, string> properties = new RLinkedHashMap<string, string>();

    public TransportContext(IObject container) {
        super(container);
    }

    public TransportContext() {}

    public synchronized boolean hasProperty(String key) {
        return properties.containsKey(new string(key));
    }

    public synchronized String getProperty(String key) {
        return properties.get(new string(key)).get();
    }

    public synchronized void setProperty(String key, String value) {
        properties.put(new string(key), new string(value));
    }

    public synchronized bool z8_hasProperty(string key) {
        return new bool(properties.containsKey(key));
    }

    public synchronized string z8_getProperty(string key) {
        return properties.get(key);
    }

    public synchronized void z8_setProperty(string key, string value) {
        properties.put(key, value);
    }

}
