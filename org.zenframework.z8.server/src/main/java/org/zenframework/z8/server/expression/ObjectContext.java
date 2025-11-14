package org.zenframework.z8.server.expression;

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
}
