package org.zenframework.z8.server.expression;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.expression.function.Format;
import org.zenframework.z8.server.expression.function.Function;

public class Functions {

	private static final Map<String, Function> Functions = new HashMap<String, Function>();

	public static void register(String name, Function function) {
		Functions.put(name, function);
	}

	static {
		register(Format.NAME, new Format());
	}

	public static Object call(String name, Object... arguments) {
		Function function = Functions.get(name);

		if (function == null)
			throw new RuntimeException("Unknown function '" + name + "'");

		return function.call(arguments);
	}
}
