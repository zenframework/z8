package org.zenframework.z8.server.utils;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class PrimaryUtils {

	private PrimaryUtils() {}

	public static Map<String, String> unwrapStringMap(Map<string, string> map) {
		Map<String, String> result = new HashMap<String, String>();
		for (Map.Entry<string, string> entry : map.entrySet())
			result.put(entry.getKey().get(), entry.getValue().get());
		return result;
	}

	public static primary toPrimary(Object v) {
		if (v == null)
			return null;
		if (v instanceof primary)
			return (primary) v;
		if (v instanceof Boolean)
			return new bool((Boolean) v);
		if (v instanceof Date)
			return new date((Date) v);
		if (v instanceof GregorianCalendar)
			return new date((GregorianCalendar) v);
		if (v instanceof Float)
			return new decimal((Float) v);
		if (v instanceof Double)
			return new decimal((Double) v);
		if (v instanceof BigDecimal)
			return new decimal((BigDecimal) v);
		if (v instanceof File)
			return new file((File) v);
		if (v instanceof UUID)
			return new guid((UUID) v);
		if (v instanceof Byte)
			return new integer((Byte) v);
		if (v instanceof Short)
			return new integer((Short) v);
		if (v instanceof Integer)
			return new integer((Integer) v);
		if (v instanceof Long)
			return new integer((Long) v);
		return new string(v.toString());
	}

}
