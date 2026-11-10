package org.zenframework.z8.server.runtime;

import org.zenframework.z8.server.types.primary;

public class META {

	public static enum Type {
		Primary, Object, Array, Map
	}

	private final Type type;
	private final Class<?> javaClass;
	private final META keyMeta;
	private final META valueMeta;

	private META(Type type, Class<?> javaClass, META keyMeta, META valueMeta) {
		this.type = type;
		this.javaClass = javaClass;
		this.keyMeta = keyMeta;
		this.valueMeta = valueMeta;
	}

	public Type getType() {
		return type;
	}

	public Class<?> getJavaClass() {
		return javaClass;
	}

	public META getKeyMeta() {
		return keyMeta;
	}

	public META getValueMeta() {
		return valueMeta;
	}

	@SuppressWarnings({ "rawtypes" })
	public Object newObject() {
		switch (type) {
		case Array:
			return new RCollection().setMeta(this);
		case Map:
			return new RLinkedHashMap().setMeta(this);
		case Primary:
			try {
				return javaClass.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		case Object:
			try {
				return javaClass.getConstructor(IObject.class).newInstance((OBJECT) null);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		default:
			throw new IllegalStateException("Unknown type " + type);
		}
	}

	public static META meta(Class<?> javaClass) {
		if (primary.class.isAssignableFrom(javaClass))
			return new META(Type.Primary, javaClass, null, null);
		if (CLASS.class.isAssignableFrom(javaClass))
			return new META(Type.Object, javaClass, null, null);
		throw new IllegalStateException("Unexpected Java class: " + javaClass.getName());
	}

	@SuppressWarnings("unchecked")
	public static META meta(Class<?> javaClass, Class<? extends primary>... keys) {
		META meta = meta(javaClass);

		for (Class<? extends primary> key : keys)
			meta = key == null ? new META(Type.Array, RCollection.class, null, meta)
					: new META(Type.Array, RCollection.class, meta(key), meta);

		return meta;
	}

	@SuppressWarnings("rawtypes")
	public static META get(Object value) {
		if (value instanceof primary)
			return ((primary) value).getMeta();
		if (value instanceof CLASS)
			return ((CLASS) value).getMeta();
		if (value instanceof RCollection)
			return ((RCollection) value).getMeta();
		if (value instanceof RLinkedHashMap)
			return ((RLinkedHashMap) value).getMeta();
		throw new IllegalStateException("Object " + value + " has no META");
	}

}
