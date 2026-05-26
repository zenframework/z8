package org.zenframework.z8.server.expression.function;

import java.lang.reflect.Method;

public class MethodWrapper extends Function {

	private final Object object;
	private final Method method;

	public MethodWrapper(Object object, Method method) {
		this.object = object;
		this.method = method;
	}

	@Override
	public Object call(Object... arguments) {
		try {
			return method.invoke(object, arguments);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

}
