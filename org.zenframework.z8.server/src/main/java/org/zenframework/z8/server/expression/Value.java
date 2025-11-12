package org.zenframework.z8.server.expression;

import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Value {

	private Object value;
	private boolean evaluated;

	public Object get() {
		return value;
	}

	public Value setValue(Object value) {
		this.evaluated = true;
		this.value = value;
		return this;
	}

	public Value setExpression(String expression) {
		this.evaluated = false;
		this.value = expression;
		return this;
	}

	public boolean isEvaluated() {
		return evaluated;
	}

	@Override
	public String toString() {
		return value == null ? "null" : !evaluated ? value.toString()
				: value instanceof string ? quote(((string) value).get())
				: value instanceof primary ? value.toString() : quote(value.toString());
	}

	public static boolean isEvaluated(Value[] values) {
		for (Value value : values)
			if (!value.isEvaluated())
				return false;
		return true;
	}

	public static Object[] get(Value[] values) {
		Object[] result = new Object[values.length];

		for (int i = 0; i < values.length; i++)
			result[i] = values[i].get();

		return result;
	}

	public static String toString(Value[] values) {
		StringBuilder str = new StringBuilder(1024);

		for (int i = 0; i < values.length; i++) {
			if (i > 0)
				str.append(", ");
			str.append(values[i].get());
		}

		return str.toString();
	}

	private static String quote(String value) {
		return "\"" + value.replace("\"", "\\\"") + '"';
	}
}
