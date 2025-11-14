package org.zenframework.z8.server.expression;

public abstract class Context {

	private final Context parent;

	public Context(Context parent) {
		this.parent = parent;
	}

	public final Variable getVariable(String name) {
		Variable variable = getDefinedVariable(name);
		return variable != null ? variable : parent != null ? parent.getVariable(name) : null;
	}

	protected abstract Variable getDefinedVariable(String name);
}
