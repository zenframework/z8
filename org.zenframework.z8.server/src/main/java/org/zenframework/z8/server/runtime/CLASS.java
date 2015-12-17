package org.zenframework.z8.server.runtime;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.logs.Trace;

public class CLASS<TYPE extends IObject> extends OBJECT implements IClass<TYPE> {
    public final static int Constructor = -1;
    public final static int Constructor1 = 0;
    public final static int Constructor2 = 1;

    private Class<TYPE> javaClass;
    private TYPE object = null;
    private int stage = Constructor;

    Constructor<?> constructor = null;

    private List<IClass<TYPE>> references = null;

    static public <T extends IObject> List<T> asList(
            Collection<? extends org.zenframework.z8.server.runtime.CLASS<? extends T>> collection) {
        return asList(collection, false);
    }

    static public <T extends IObject> List<T> asList(
            Collection<? extends org.zenframework.z8.server.runtime.CLASS<? extends T>> collection, boolean createNewInstances) {
        List<T> result = new ArrayList<T>();

        if (collection != null) {
            for (org.zenframework.z8.server.runtime.CLASS<? extends T> cls : collection) {
                result.add((T) (createNewInstances ? cls.newInstance() : cls.get()));
            }
        }

        return result;
    }

    public CLASS(IObject container) {
        super(container);
    }

    @Override
    final public Class<TYPE> getJavaClass() {
        return javaClass;
    }

    @Override
    @SuppressWarnings("unchecked")
    final public void setJavaClass(Class<?> cls) {
        assert (object == null);
        javaClass = (Class<TYPE>)cls;
    }

    @SuppressWarnings("unchecked")
    public void setObject(IObject object) {
        this.object = (TYPE) object;
        this.stage = Constructor2;

        this.object.setContainer(getContainer());
        setAttributes(object.getAttributes());

        this.object.setCLASS(this);
        this.object.setIndex(getIndex());
    }

    @Override
    public List<IClass<TYPE>> getReferences() {
        if (references == null) {
            references = new ArrayList<IClass<TYPE>>();
        }

        return references;
    }

    public void addReference(IClass<TYPE> reference) {
        if (references == null) {
            references = new ArrayList<IClass<TYPE>>();
        }

        if (!references.contains(reference)) {
            references.add(reference);
        }
    }

    public boolean instanceOf(Class<?> cls) {
        return cls.isAssignableFrom(getJavaClass());
    }

    @Override
    public boolean hasInstance() {
        return object != null;
    }

    @Override
    public final TYPE get() {
        return get(Constructor2);
    }

    public/* final */TYPE get(int stage) {
        if (object == null) {
            create(getContainer(), stage);
        }

        callConstructors(stage);

        return object;
    }

    @Override
    public String getAttribute(String key) {
        return (object != null ? object.getAttribute(key) : super.getAttribute(key));
    }

    @Override
    public void setAttribute(String key, String value) {
        if (object != null) {
            object.setAttribute(key, value);
        }
        super.setAttribute(key, value);
    }

    @Override
    public void removeAttribute(String key) {
        if (object != null) {
            object.removeAttribute(key);
        }
        super.removeAttribute(key);
    }

    @Override
    public boolean hasAttribute(String key) {
        return (object != null ? object.hasAttribute(key) : super.hasAttribute(key));
    }

    @Override
    public String displayName() {
        String name = super.displayName();
        if (name == null || name.isEmpty()) {
            return getJavaClass().getSimpleName();
        }
        return name;
    }

    public Object newObject(IObject container) {
        return null;
    }

    @SuppressWarnings("unchecked")
    private TYPE constructObject(IObject container) throws Exception {
        TYPE object = (TYPE) newObject(container);

        if (object != null) {
            return object;
        }

        try {
            Constructor<TYPE> constructor = javaClass.getDeclaredConstructor(IObject.class);
            return (TYPE) constructor.newInstance(container);
        } catch (NoSuchMethodException e) {
            Class<?> enclosingClass = javaClass.getEnclosingClass();
            Constructor<TYPE> constructor = javaClass.getDeclaredConstructor(enclosingClass, IObject.class);
            constructor.setAccessible(true);
            return (TYPE) constructor.newInstance(container, container);
        }
    }

    private void create(IObject container, int stage) {
        assert (object == null);

        try {
            object = constructObject(container);
            object.setCLASS(this);

            callConstructors(stage);
        } catch (Throwable e) {
            Trace.logError(e);
            throw new RuntimeException(e);
        }
    }

    private void callConstructors(int stage) {
        if (object != null && this.stage < Constructor1 && stage >= Constructor1) {
            this.stage = Constructor1;
            object.constructor1();
            object.setAttributes(getAttributes());
        }

        callConstructor2(stage);
    }

    private void callConstructor2(int stage) {
        if (object != null && this.stage < Constructor2 && stage >= Constructor2) {
            this.stage = Constructor2;
            object.constructor2();
            object.onInitialized();

            object.constructor();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public TYPE newInstance() {
        try {
            if(constructor == null)
                constructor = getClass().getDeclaredConstructor(IObject.class);
            org.zenframework.z8.server.runtime.CLASS<TYPE> cls = (org.zenframework.z8.server.runtime.CLASS<TYPE>) constructor.newInstance(getContainer());
            return cls.get();
        } catch (Throwable e) {
            Trace.logError(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String classId() {
        assert (javaClass != null);
        return javaClass.getCanonicalName();
    }

}
