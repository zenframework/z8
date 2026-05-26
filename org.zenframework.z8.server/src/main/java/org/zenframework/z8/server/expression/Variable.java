package org.zenframework.z8.server.expression;

public class Variable {

	private final String name;
	private final Object value;

	public Variable(String name) {
		this(name, null);
	}

	public Variable(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		Object value = getValue();
		return value != null ? value.toString() : "null";
	}
}
