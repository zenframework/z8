package org.zenframework.z8.server.expression;

import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.runtime.OBJECT;

public class ObjectContext implements Context {

	private final OBJECT object;

	public ObjectContext(OBJECT object) {
		this.object = object;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Variable getVariable(String name) {
		CLASS value = (CLASS) object.getMember(name);
		return value != null ? new Variable(name, value.get()) : null;
	}
}
