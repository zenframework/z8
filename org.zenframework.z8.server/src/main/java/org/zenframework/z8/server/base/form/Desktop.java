package org.zenframework.z8.server.base.form;

import java.util.Collection;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.simple.Runnable;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;

public class Desktop extends OBJECT {
    public static class CLASS<T extends Desktop> extends OBJECT.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(Desktop.class);
            setAttribute(Native, Desktop.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new Desktop(container);
        }
    }

    protected RCollection<Runnable.CLASS<? extends Runnable>> runnables = new RCollection<Runnable.CLASS<? extends Runnable>>();
    protected RCollection<Desktop.CLASS<? extends Desktop>> subDesktops = new RCollection<Desktop.CLASS<? extends Desktop>>();
    protected RCollection<Query.CLASS<? extends Query>> dataSets = new RCollection<Query.CLASS<? extends Query>>();

    public Desktop(IObject container) {
        super(container);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Collection<Runnable.CLASS> getRunnables() {
        return (Collection)runnables;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Collection<Desktop.CLASS> getSubDesktops() {
        return (Collection)subDesktops;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Collection<Query.CLASS> getDataSets() {
        return (Collection)dataSets;
    }
}
