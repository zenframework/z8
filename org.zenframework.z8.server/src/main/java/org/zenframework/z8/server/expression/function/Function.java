package org.zenframework.z8.server.expression.function;

public abstract class Function {

	public abstract Object call(Object... arguments);

	@SuppressWarnings("unchecked")
	protected static <T> T checkType(Object value, Class<T> cls) {
		if (value != null && !cls.isInstance(value))
			throw new RuntimeException("Illegal argument type: " + value.getClass().getCanonicalName() + ", but " + cls.getCanonicalName() + " expected");
		return (T) value;
	}

}
