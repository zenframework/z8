package org.zenframework.z8.server.json.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class JsonObject extends HashMap<String, Object> {
	private static final long serialVersionUID = -4995973267370970302L;

	public JsonObject() {
	}

	public JsonObject(JsonTokener x) {
		this();
		char c;
		String key;

		if(x.nextClean() != '{')
			x.syntaxError("A JSONObject text must begin with '{'");

		for(;;) {
			c = x.nextClean();
			switch(c) {
			case 0:
				x.syntaxError("A JSONObject text must end with '}'");
			case '}':
				return;
			default:
				x.back();
				key = x.nextValue().toString();
			}

			// The key is followed by ':'. We will also tolerate '=' or '=>'.

			c = x.nextClean();
			if(c == '=') {
				if(x.next() != '>')
					x.back();
			} else if(c != ':')
				x.syntaxError("Expected a ':' after a key");

			Object value = x.nextValue();
			if(key != null)
				put(key, value);

			// Pairs are separated by ','. We will also tolerate ';'.

			switch(x.nextClean()) {
			case ';':
			case ',':
				if(x.nextClean() == '}')
					return;
				x.back();
				break;
			case '}':
				return;
			default:
				x.syntaxError("Expected a ',' or '}'");
			}
		}
	}

	public JsonObject(Map<?, ?> map) {
		if(map != null) {
			for(Map.Entry<?, ?> e : map.entrySet()) {
				Object value = e.getValue();
				if(value != null)
					super.put(e.getKey().toString(), wrap(value));
			}
		}
	}

	public JsonObject(String source) {
		this(new JsonTokener(source == null || source.trim().isEmpty() ? "{}" : source));
	}

	public static String doubleToString(double d) {
		if(Double.isInfinite(d) || Double.isNaN(d))
			return "null";

		// Shave off trailing zeros and decimal point, if possible.
		String string = Double.toString(d);
		if(string.indexOf('.') > 0 && string.indexOf('e') < 0 && string.indexOf('E') < 0) {
			while(string.endsWith("0"))
				string = string.substring(0, string.length() - 1);

			if(string.endsWith("."))
				string = string.substring(0, string.length() - 1);
		}
		return string;
	}

	public bool getBool(string key) {
		return getBool(key.get());
	}

	public bool getBool(String key) {
		Object object = get(key);

		if(object == null || object instanceof bool)
			return (bool)object;

		return new bool(getBoolean(object));
	}

	public boolean getBoolean(string key) {
		return getBoolean(key.get());
	}

	public boolean getBoolean(String key) {
		return getBoolean(get(key));
	}

	private boolean getBoolean(Object object) {
		return object.equals(Boolean.TRUE);
	}

	public date getDate(string key) {
		return getDate(key.get());
	}

	public date getDate(String key) {
		Object object = get(key);

		if(object == null || object instanceof date)
			return (date)object;

		try {
			return new date(getLong(object));
		} catch (Exception e) {
			return date.parse(object.toString());
		}
	}

	public guid getGuid(string key) {
		return getGuid(key.get());
	}

	public guid getGuid(String key) {
		Object object = get(key);

		if(object == null || object instanceof guid)
			return (guid)object;

		return new guid(object.toString());
	}

	public decimal getDecimal(string key) {
		return getDecimal(key.get());
	}
	
	public decimal getDecimal(String key) {
		Object object = get(key);

		if(object == null || object instanceof integer)
			return (decimal)object;

		return new decimal(getDouble(object));
	}

	public double getDouble(string key) {
		return getDouble(key.get());
	}

	public double getDouble(String key) {
		return getDouble(get(key));
	}

	private double getDouble(Object object) {
		return object instanceof Number ? ((Number)object).doubleValue() : Double.parseDouble((String)object);
	}

	public integer getInteger(string key) {
		return getInteger(key.get());
	}

	public integer getInteger(String key) {
		Object object = get(key);

		if(object == null || object instanceof integer)
			return (integer)object;

		return new integer(getLong(object));
	}

	public int getInt(string key) {
		return getInt(key.get());
	}

	public int getInt(String key) {
		return getInt(get(key));
	}

	private int getInt(Object object) {
		return object instanceof Number ? ((Number)object).intValue() : Integer.parseInt((String)object);
	}

	public long getLong(string key) {
		return getLong(key.get());
	}

	public long getLong(String key) {
		return getLong(get(key));
	}

	private long getLong(Object object) {
		return object instanceof Number ? ((Number)object).longValue() : Long.parseLong((String)object);
	}

	public JsonArray getJsonArray(string key) {
		return getJsonArray(key.get());
	}

	public JsonArray getJsonArray(String key) {
		return (JsonArray)get(key);
	}

	public JsonObject getJsonObject(string key) {
		return getJsonObject(key.get());
	}

	public JsonObject getJsonObject(String key) {
		return (JsonObject)get(key);
	}

	public boolean isNull(String key) {
		return get(key) == null;
	}

	public Set<String> getNames() {
		return keySet();
	}

	public String getString(string key) {
		return getString(key.get());
	}

	public String getString(String key) {
		Object object = get(key);
		return object == null ? null : object.toString();
	}

	public boolean has(string key) {
		return has(key.get());
	}

	public boolean has(String key) {
		return super.containsKey(key);
	}

	public static String numberToString(Number number) {
		String string = number.toString();
		if(string.indexOf('.') > 0 && string.indexOf('e') < 0 && string.indexOf('E') < 0) {
			while(string.endsWith("0"))
				string = string.substring(0, string.length() - 1);

			if(string.endsWith("."))
				string = string.substring(0, string.length() - 1);
		}
		return string;
	}

	public JsonObject set(String key, Collection<?> value) {
		super.put(key, (Object)(value instanceof JsonArray ? value : new JsonArray(value)));
		return this;
	}

	public JsonObject set(string key, Collection<?> value) {
		return set(key.get(), value);
	}

	public JsonObject set(String key, Map<?, ?> value) {
		set(key, (Object)(value instanceof JsonObject ? value : new JsonObject(value)));
		return this;
	}

	public JsonObject set(string key, Map<?, ?> value) {
		return set(key.get(), value);
	}

	public JsonObject set(String key, Object value) {
		return put(key, value);
	}

	@Override
	public JsonObject put(String key, Object value) {
		super.put(key, wrap(value));
		return this;
	}

	public JsonObject set(string key, Object value) {
		return put(key.get(), value);
	}

	public JsonObject addAll(JsonObject obj) {
		putAll((Map<String, Object>)obj);
		return this;
	}

	public static String quote(String string) {
		if(string == null)
			return null;

		if(string.length() == 0)
			return "\"\"";

		char b;
		char c = 0;
		String hhhh;
		int i;

		int len = string.length();
		StringBuilder sb = new StringBuilder(len + 4);

		sb.append('"');
		for(i = 0; i < len; i += 1) {
			b = c;
			c = string.charAt(i);
			switch(c) {
			case '\\':
			case '"':
				sb.append('\\');
				sb.append(c);
				break;
			case '/':
				if(b == '<') {
					sb.append('\\');
				}
				sb.append(c);
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\r':
				sb.append("\\r");
				break;
			default:
				if(c < ' ' || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) {
					hhhh = "000" + Integer.toHexString(c);
					sb.append("\\u" + hhhh.substring(hhhh.length() - 4));
				} else
					sb.append(c);
			}
		}
		sb.append('"');
		return sb.toString();
	}

	public static Object stringToValue(String string) {
		if(string.equals(""))
			return string;

		if(string.equalsIgnoreCase("true"))
			return Boolean.TRUE;

		if(string.equalsIgnoreCase("false"))
			return Boolean.FALSE;

		if(string.equalsIgnoreCase("null"))
			return null;

		/*
		 * If it might be a number, try converting it. We support the
		 * non-standard 0x- convention. If a number cannot be produced, then the
		 * value will just be a string. Note that the 0x-, plus, and implied
		 * string conventions are non-standard. A JSON parser may accept
		 * non-JSON forms as long as it accepts all correct JSON forms.
		 */

		char b = string.charAt(0);
		if((b >= '0' && b <= '9') || b == '.' || b == '-' || b == '+') {
			if(b == '0' && string.length() > 2 && (string.charAt(1) == 'x' || string.charAt(1) == 'X')) {
				try {
					return new Integer(Integer.parseInt(string.substring(2), 16));
				} catch(Exception ignore) {
				}
			}
			try {
				if(string.indexOf('.') > -1 || string.indexOf('e') > -1 || string.indexOf('E') > -1)
					return Double.valueOf(string);
				else {
					Long myLong = new Long(string);
					if(myLong.longValue() == myLong.intValue())
						return new Integer(myLong.intValue());
					else
						return myLong;
				}
			} catch(Exception ignore) {
			}
		}
		return string;
	}
	@Override
	public String toString() {
		try {
			Iterator<String> keys = keySet().iterator();
			StringBuilder sb = new StringBuilder(1024).append("{");

			while(keys.hasNext()) {
				if(sb.length() > 1)
					sb.append(',');

				Object o = keys.next();
				sb.append(quote(o.toString()));
				sb.append(':');
				sb.append(valueToString(super.get(o)));
			}
			sb.append('}');
			return sb.toString();
		} catch(Exception e) {
			return null;
		}
	}

	public static String valueToString(Object value) {
		if(value == null)
			return "null";

		// convert primary to Java native
		if(value instanceof bool)
			return Boolean.toString(((bool)value).get());
		else if(value instanceof integer)
			return numberToString(((integer)value).get());
		else if(value instanceof decimal)
			return numberToString(((decimal)value).get());
		else if(value instanceof date) {
			date dt = (date)value;
			boolean minMax = dt.equals(date.Min) || dt.equals(date.Max);
			return minMax ? "null" : value.toString();
		} else if(value instanceof JsonString)
			return ((JsonString)value).toJSONString();
		else if(value instanceof Number)
			return numberToString((Number)value);
		else if(value instanceof Boolean || value instanceof JsonObject || value instanceof JsonArray)
			return value.toString();
		else if(value instanceof Map)
			return new JsonObject((Map<?, ?>)value).toString();
		else if(value instanceof Collection)
			return new JsonArray((Collection<?>)value).toString();
		else if(value.getClass().isArray())
			return new JsonArray(value).toString();
		else
			return quote(value.toString());
	}

	public static Object wrap(Object object) {
		if(object == null || object instanceof JsonObject || object instanceof JsonArray || object instanceof JsonString || object instanceof Byte ||
				object instanceof Character || object instanceof Short || object instanceof Integer || object instanceof Long ||
				object instanceof Boolean || object instanceof Float || object instanceof Double || object instanceof String)
			return object;

		if(object instanceof Collection)
			return new JsonArray((Collection<?>)object);

		if(object.getClass().isArray())
			return new JsonArray(object);

		if(object instanceof Map)
			return new JsonObject((Map<?, ?>)object);

		if(object instanceof primary)
			return JsonUtils.unwrap(object);

		return object;
	}
}
