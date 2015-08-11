package org.zenframework.z8.server.ie;

import java.io.Serializable;

import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.string;

public class TransportContext extends OBJECT implements Serializable {

    private static final long serialVersionUID = 3103056587172568570L;

    public static final String SelfAddressProperty = "selfAddress";

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

    private static class SelfAddressDefaultListener implements Properties.Listener {

        @Override
        public void onPropertyChange(String key, String value) {
            if (ServerRuntime.SelfAddressDefaultProperty.equalsKey(key)) {
                selfAddressDefault = value;
            }
        }

    }

    static {
        Properties.addListener(new SelfAddressDefaultListener());
    }

    private static volatile String selfAddressDefault = Properties.getProperty(ServerRuntime.SelfAddressDefaultProperty);

    private final RLinkedHashMap<string, string> properties = new RLinkedHashMap<string, string>();

    public TransportContext(IObject container) {
        super(container);
    }

    @Override
    public void constructor2() {
        super.constructor2();
        if (selfAddressDefault != null && !selfAddressDefault.isEmpty()) {
            setProperty(SelfAddressProperty, selfAddressDefault);
        }
    }

    public TransportContext check() {
        if (!hasProperty(SelfAddressProperty)) {
            throw new exception("Transport context property '" + TransportContext.SelfAddressProperty + "' is not set");
        }
        return this;
    }

    public boolean hasProperty(String key) {
        return properties.containsKey(new string(key));
    }

    public String getProperty(String key) {
        return properties.z8_get(new string(key)).get();
    }

    public void setProperty(String key, String value) {
        properties.put(new string(key), new string(value));
    }

    @SuppressWarnings("unchecked")
    public TransportContext.CLASS<? extends TransportContext> z8_check() {
        check();
        return (TransportContext.CLASS<? extends TransportContext>) getCLASS();
    }

    public bool z8_hasProperty(string key) {
        return new bool(properties.containsKey(key));
    }

    public string z8_getProperty(string key) {
        return properties.get(key);
    }

    public void z8_setProperty(string key, string value) {
        properties.put(key, value);
    }

}
