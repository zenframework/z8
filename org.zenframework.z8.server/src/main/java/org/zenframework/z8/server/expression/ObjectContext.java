package org.zenframework.z8.server.expression;

import java.lang.reflect.Method;

import org.zenframework.z8.server.expression.function.Function;
import org.zenframework.z8.server.expression.function.MethodWrapper;
import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.runtime.OBJECT;

public class ObjectContext extends Context {

	private final OBJECT object;

	public ObjectContext(Context parent, OBJECT object) {
		super(parent);
		this.object = object;
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected Variable getDefinedVariable(String name) {
		CLASS value = (CLASS) object.getMember(name);
		return value != null ? new Variable(name, value.get()) : null;
	}

	@Override
	protected Function getDefinedFunction(String name) {
		Method method = getMethod(name);
		return method != null ? new MethodWrapper(object, method) : null;
	}

	private Method getMethod(String name) {
		Method found = null;

		for (Method method : object.getClass().getMethods()) {
			if (method.getName().equals(name)) {
				if (found != null)
					throw new IllegalStateException("Method " + name + "() is ambiguous");
				found = method;
			}
		}

		return found;
	}
}
