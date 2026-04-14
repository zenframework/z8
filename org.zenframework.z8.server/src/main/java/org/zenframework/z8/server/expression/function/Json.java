package org.zenframework.z8.server.expression.function;

import java.util.Collection;
import java.util.Map;

import org.zenframework.z8.server.expression.Context;
import org.zenframework.z8.server.expression.DefaultContext;
import org.zenframework.z8.server.json.parser.JsonUtils;
import org.zenframework.z8.server.types.string;

public class Json {

	public static class Min extends Function {
		public static final String NAME = "min";

		@Override
		public Object call(Object... arguments) {
			if (arguments.length != 2)
				throw new IllegalArgumentException("min: illegal arguments");

			Object array = arguments[0];
			string key = checkType(arguments[1], string.class);

			if (array == null)
				return null;

			Collection<Object> list = getCollection(array);

			Number min = Long.MAX_VALUE;

			for (Object e : list) {
				Object value = getValue(e, key);

				if (!(value instanceof Number))
					value = Long.MIN_VALUE;

				min = min(min, (Number) value);
			}

			return JsonUtils.wrap(min);
		}
	}

	public static class Max extends Function {
		public static final String NAME = "max";

		@Override
		public Object call(Object... arguments) {
			if (arguments.length != 2)
				throw new IllegalArgumentException("min: illegal arguments");

			Object array = arguments[0];
			string key = checkType(arguments[1], string.class);

			if (array == null)
				return null;

			Collection<Object> list = getCollection(array);

			Number max = Long.MIN_VALUE;

			for (Object e : list) {
				Object value = getValue(e, key);

				if (!(value instanceof Number))
					value = Long.MAX_VALUE;

				max = max(max, (Number) value);
			}

			return JsonUtils.wrap(max);
		}
	}

	public static Context create() {
		return new DefaultContext(null).setFunction(Min.NAME, new Min()).setFunction(Max.NAME, new Max());
	}

	@SuppressWarnings("unchecked")
	private static Collection<Object> getCollection(Object array) {
		if (array instanceof org.zenframework.z8.server.base.json.parser.JsonObject)
			array = ((org.zenframework.z8.server.base.json.parser.JsonObject) array).get();

		if (array instanceof org.zenframework.z8.server.base.json.parser.JsonArray)
			array = ((org.zenframework.z8.server.base.json.parser.JsonArray) array).get();

		if (array instanceof org.zenframework.z8.server.json.parser.JsonObject)
			array = ((org.zenframework.z8.server.json.parser.JsonObject) array).values();

		if (!(array instanceof Collection))
			throw new IllegalArgumentException(array.toString());

		return (Collection<Object>) array;
	}

	private static Number min(Number a, Number b) {
		if (a == null)
			return b;
		if (b == null)
			return a;
		if (a instanceof Float || a instanceof Double || b instanceof Float || b instanceof Double)
			return a.doubleValue() < b.doubleValue() ? a : b;
		return a.longValue() < b.longValue() ? a : b;
	}

	private static Number max(Number a, Number b) {
		if (a == null)
			return b;
		if (b == null)
			return a;
		if (a instanceof Float || a instanceof Double || b instanceof Float || b instanceof Double)
			return a.doubleValue() > b.doubleValue() ? a : b;
		return a.longValue() > b.longValue() ? a : b;
	}

	@SuppressWarnings("unchecked")
	private static Object getValue(Object o, Object key) {
		if (o == null || !(o instanceof Map))
			return null;

		return ((Map<String, Object>) o).get(key.toString());
	}
}
