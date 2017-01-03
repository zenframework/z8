package org.zenframework.z8.server.runtime;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CLASS<TYPE extends IObject> extends OBJECT implements IClass<TYPE> {
	public final static int Constructor = -1;
	public final static int Constructor1 = 0;
	public final static int Constructor2 = 1;

	private Class<TYPE> javaClass;
	private String classId;

	private TYPE object = null;
	private int stage = Constructor;

	Constructor<?> constructor = null;

	private List<IClass<TYPE>> references = null;

	static public <T extends IObject> List<T> asList(Collection<? extends org.zenframework.z8.server.runtime.CLASS<? extends T>> collection) {
		List<T> result = new ArrayList<T>();

		if(collection != null) {
			for(org.zenframework.z8.server.runtime.CLASS<? extends T> cls : collection)
				result.add((T)(cls.get()));
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
		javaClass = (Class<TYPE>)cls;
	}

	@Override
	public void resetId() {
		super.resetId();

		if(object != null)
			object.resetId();
	}

	@Override
	public void setContainer(IObject container) {
		super.setContainer(container);

		if(object != null)
			object.setContainer(container);
	}

	@Override
	public void setOwner(IObject owner) {
		super.setOwner(owner);

		if(object != null)
			object.setOwner(owner);
	}

	@Override
	public void setOrdinal(int ordinal) {
		super.setOrdinal(ordinal);

		if(object != null)
			object.setOrdinal(ordinal);
	}

	@Override
	public List<IClass<TYPE>> getReferences() {
		if(references == null)
			references = new ArrayList<IClass<TYPE>>();

		return references;
	}

	public void addReference(IClass<TYPE> reference) {
		if(references == null)
			references = new ArrayList<IClass<TYPE>>();

		if(!references.contains(reference))
			references.add(reference);
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
		if(object != null && this.stage >= stage)
			return object;

		if(object == null)
			create(getContainer());

		callConstructors(stage);

		return object;
	}

	public Object newObject(IObject container) {
		return null;
	}

	@SuppressWarnings("unchecked")
	private TYPE constructObject(IObject container) throws Exception {
		TYPE object = (TYPE)newObject(container);

		if(object != null)
			return object;

		try {
			Constructor<TYPE> constructor = javaClass.getDeclaredConstructor(IObject.class);
			return (TYPE)constructor.newInstance(container);
		} catch(NoSuchMethodException e) {
			Class<?> enclosingClass = javaClass.getEnclosingClass();
			Constructor<TYPE> constructor = javaClass.getDeclaredConstructor(enclosingClass, IObject.class);
			constructor.setAccessible(true);
			return (TYPE)constructor.newInstance(container, container);
		}
	}

	private void create(IObject container) {
		try {
			object = constructObject(container);
			object.setCLASS(this);
			object.setOwner(getOwner());
			object.setOrdinal(ordinal());
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private void callConstructors(int stage) {
		if(this.stage < Constructor1 && stage >= Constructor1) {
			this.stage = Constructor1;
			object.constructor1();
			object.setAttributes(getAttributes());
		}

		callConstructor2(stage);
	}

	private void callConstructor2(int stage) {
		if(this.stage < Constructor2 && stage >= Constructor2) {
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
			org.zenframework.z8.server.runtime.CLASS<TYPE> cls = (org.zenframework.z8.server.runtime.CLASS<TYPE>)constructor.newInstance(getContainer());
			return cls.get();
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String classId() {
		return classId == null ? classId = javaClass.getCanonicalName() : classId;
	}
}
