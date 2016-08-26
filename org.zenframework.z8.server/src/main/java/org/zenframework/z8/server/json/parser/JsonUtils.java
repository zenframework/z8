package org.zenframework.z8.server.json.parser;

import java.math.BigDecimal;
import java.util.GregorianCalendar;
import java.util.UUID;

import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class JsonUtils {
	public static Object unwrap(Object o) {
		if(o instanceof bool) {
			return ((bool)o).get();
		} else if(o instanceof date) {
			date dt = (date)o;
			boolean minMax = dt.equals(date.MIN) || dt.equals(date.MAX);
			return minMax ? "" : dt.toString();
		} else if(o instanceof datespan)
			return ((datespan)o).get();
		else if(o instanceof decimal)
			return ((decimal)o).get();
		else if(o instanceof guid)
			return ((guid)o).toString();
		else if(o instanceof integer)
			return ((integer)o).get();
		else if(o instanceof string)
			return ((string)o).get();
		else if(o instanceof file)
			return ((file)o).toJsonObject();
		else if(o instanceof binary)
			throw new UnsupportedOperationException();
		else
			return o;
	}

	public static primary wrap(Object o) {
		if(o instanceof primary)
			return (primary)o;
		else if(o instanceof Boolean)
			return new bool((Boolean)o);
		else if(o instanceof GregorianCalendar)
			return new date((GregorianCalendar)o);
		else if(o instanceof Float || o instanceof Double)
			return new decimal((Double)o);
		else if(o instanceof BigDecimal)
			return new decimal((BigDecimal)o);
		else if(o instanceof UUID)
			return new guid((UUID)o);
		else if(o instanceof Byte || o instanceof Short || o instanceof Integer || o instanceof Long)
			return new integer((Long)o);
		else if(o instanceof String)
			return new string((String)o);
		else
			throw new UnsupportedOperationException();
	}
}