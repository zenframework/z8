package org.zenframework.z8.server.base.simple;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;

public class Runnable extends OBJECT {
    public static class CLASS<T extends Runnable> extends OBJECT.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(Runnable.class);
            setAttribute(Native, Runnable.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new Runnable(container);
        }
    }

    public Runnable(IObject container) {
        super(container);
    }

    public void run() {}

}
