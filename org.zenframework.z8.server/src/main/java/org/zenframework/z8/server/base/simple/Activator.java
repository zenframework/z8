package org.zenframework.z8.server.base.simple;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;

public class Activator extends OBJECT {

    public static class CLASS<T extends Activator> extends OBJECT.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(Activator.class);
            setAttribute(Native, Activator.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new Activator(container);
        }
    }

    public Activator(IObject container) {
        super(container);
    }

    public void onInitialize() {
        z8_onInitialize();
    }

    public void afterDbGenerated() {
        z8_afterDbGenerated();
    }

    public void z8_onInitialize() {}

    public void z8_afterDbGenerated() {}

}
