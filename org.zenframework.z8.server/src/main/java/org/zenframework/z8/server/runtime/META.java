package org.zenframework.z8.server.runtime;

import java.lang.reflect.Field;

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

	public static META meta(Class<?> javaClass, Class<?>... keys) {
		META meta = meta(javaClass);

		for (Class<?> key : keys)
			meta = key == null ? new META(Type.Array, RCollection.class, null, meta)
					: new META(Type.Array, RCollection.class, meta(key), meta);

		return meta;
	}

	public static META get(Object object, Field field) {
		Class<?> type = field.getType();

		if (!(Any.class.isAssignableFrom(type)))
			throw new IllegalArgumentException();

		try {
			Object value = field.get(object);
			if (value != null)
				return ((Any) value).getMeta();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return get(type);
	}

	@SuppressWarnings("rawtypes")
	public static META get(Class<?> type) {
		try {
			if (CLASS.class.isAssignableFrom(type))
				return ((CLASS) type.getConstructor(IObject.class).newInstance((IObject) null)).getMeta();
			if (primary.class.isAssignableFrom(type))
				return ((Any) type.newInstance()).getMeta();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		throw new IllegalArgumentException("Type " + type + " has no META");
	}

}
