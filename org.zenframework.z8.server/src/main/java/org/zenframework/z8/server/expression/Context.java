package org.zenframework.z8.server.expression;

import org.zenframework.z8.server.expression.function.Function;

public abstract class Context {

	private final Context parent;

	public Context(Context parent) {
		this.parent = parent;
	}

	public Context getParent() {
		return parent;
	}

	public final Variable getVariable(String name) {
		Variable variable = getDefinedVariable(name);
		return variable != null ? variable : parent != null ? parent.getVariable(name) : null;
	}

	public final Function getFunction(String name) {
		Function function = getDefinedFunction(name);
		return function != null ? function : parent != null ? parent.getFunction(name) : null;
	}

	protected abstract Variable getDefinedVariable(String name);
	protected abstract Function getDefinedFunction(String name);
}
