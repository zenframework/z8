package org.zenframework.z8.server.json.parser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.zenframework.z8.server.runtime.OBJECT;
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
	@SuppressWarnings("unchecked")
	public static Object unwrap(Object o) {
		if(o instanceof bool) {
			return ((bool)o).get();
		} else if(o instanceof date) {
			date dt = (date)o;
			boolean minMax = dt.equals(date.Min) || dt.equals(date.Max);
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
		else if(o instanceof org.zenframework.z8.server.base.json.parser.JsonObject.CLASS)
			return ((org.zenframework.z8.server.base.json.parser.JsonObject.CLASS<? extends org.zenframework.z8.server.base.json.parser.JsonObject>) o).get().get();
		else if(o instanceof org.zenframework.z8.server.base.json.parser.JsonArray.CLASS)
			return ((org.zenframework.z8.server.base.json.parser.JsonArray.CLASS<? extends org.zenframework.z8.server.base.json.parser.JsonArray>) o).get().get();
		else if(o instanceof Map) {
			Map<Object, Object> map = new HashMap<Object, Object>();
			for (Map.Entry<?, ?> entry : ((Map<?, ?>) o).entrySet())
				map.put(unwrap(entry.getKey()), unwrap(entry.getValue()));
			return map;
		} else if(o instanceof Collection) {
			Collection<Object> list = new ArrayList<Object>(((Collection<?>) o).size());
			for (Object e : (Collection<?>) o)
				list.add(unwrap(e));
			return list;
		} else if(o instanceof OBJECT.CLASS)
			return ((OBJECT.CLASS<? extends OBJECT>) o).get();
		return o;
	}

	public static Object wrap(Object o) {
		if (o == null || JsonObject.NULL.equals(o))
			return null;
		else if(o instanceof primary)
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
			return new integer(((Number) o).longValue());
		else if(o instanceof String)
			return new string((String)o);
		else if(o instanceof JsonObject)
			return org.zenframework.z8.server.base.json.parser.JsonObject.getJsonObject((JsonObject) o);
		else if(o instanceof JsonArray)
			return org.zenframework.z8.server.base.json.parser.JsonArray.getJsonArray((JsonArray) o);
		else
			throw new UnsupportedOperationException();
	}
}