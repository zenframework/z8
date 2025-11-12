package org.zenframework.z8.server.expression.function;

import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Format extends Function {

	public static final String NAME = "format";

	public static final String DEFAULT_FORMAT_DATE = "dd.MM.yyy";
	public static final String DEFAULT_FORMAT_DECIMAL = "% ,.2f";

	@Override
	public Object call(Object... arguments) {
		if (arguments.length != 2)
			throw new RuntimeException("format: illegal arguments");

		return format(checkType(arguments[0], primary.class), checkType(arguments[1], string.class));
	}

	public static String format(Object value) {
		return format(value, (String) null);
	}

	public static String format(Object value, string format) {
		return format(value, format != null ? format.get() : null);
	}

	public static String format(Object value, String format) {
		if (value == null)
			return "<null>";

		if (value instanceof date)
			return ((date) value).format(format != null ? format : DEFAULT_FORMAT_DATE);
		if (value instanceof decimal)
			return String.format(format != null ? format : DEFAULT_FORMAT_DECIMAL, ((decimal) value).getDouble());

		return value.toString();
	}
}
