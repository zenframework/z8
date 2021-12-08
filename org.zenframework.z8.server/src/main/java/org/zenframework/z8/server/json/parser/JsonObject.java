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

	public JsonObject(JsonTokener x) throws JsonException {
		this();
		char c;
		String key;

		if(x.nextClean() != '{')
			throw x.syntaxError("A JSONObject text must begin with '{'");

		for(;;) {
			c = x.nextClean();
			switch(c) {
			case 0:
				throw x.syntaxError("A JSONObject text must end with '}'");
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
				throw x.syntaxError("Expected a ':' after a key");

			Object value = x.nextValue();
			if(key != null && value != null)
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
				throw x.syntaxError("Expected a ',' or '}'");
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

	public JsonObject(String source) throws JsonException {
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

	public bool getBool(string key) throws JsonException {
		return getBool(key.get());
	}

	public bool getBool(String key) throws JsonException {
		Object object = get(key);

		if(object == null || object instanceof bool)
			return (bool)object;

		return getBoolean(key, object) ? bool.True : bool.False;
	}

	public boolean getBoolean(string key) throws JsonException {
		return getBoolean(key, bool.False);
	}

	public boolean getBoolean(string key, bool defaultValue) throws JsonException {
		return getBoolean(key.get(), defaultValue.get());
	}

	public boolean getBoolean(String key) throws JsonException {
		return getBoolean(key, false);
	}

	public boolean getBoolean(String key, boolean defaultValue) throws JsonException {
		Object object = get(key);
		return object != null ? getBoolean(key, object) : defaultValue;
	}

	private boolean getBoolean(String key, Object object) {
		if(object.equals(Boolean.FALSE))
			return false;
		if(object.equals(Boolean.TRUE))
			return true;
		throw new JsonException("JSONObject[" + quote(key) + "] is not a Boolean.");
	}

	public double getDouble(string key) throws JsonException {
		return getDouble(key.get());
	}

	public double getDouble(String key) throws JsonException {
		Object object = get(key);
		try {
			return object instanceof Number ? ((Number)object).doubleValue() : Double.parseDouble((String)object);
		} catch(Exception e) {
			throw new JsonException("JSONObject[" + quote(key) + "] is not a number.");
		}
	}

	public guid getGuid(string key) throws JsonException {
		return getGuid(key.get());
	}

	public guid getGuid(String key) throws JsonException {
		Object object = get(key);

		if(object == null || object instanceof guid)
			return (guid)object;

		return new guid(object.toString());
	}

	public int getInt(string key) throws JsonException {
		return getInt(key.get());
	}

	public int getInt(String key) throws JsonException {
		Object object = get(key);
		try {
			return object instanceof Number ? ((Number)object).intValue() : Integer.parseInt((String)object);
		} catch(Exception e) {
			throw new JsonException("JSONObject[" + quote(key) + "] is not an int.");
		}
	}

	public JsonArray getJsonArray(string key) throws JsonException {
		return getJsonArray(key.get());
	}

	public JsonArray getJsonArray(String key) throws JsonException {
		Object object = get(key);
		if(object instanceof JsonArray)
			return (JsonArray)object;
		throw new JsonException("JSONObject[" + quote(key) + "] is not a JSONArray.");
	}

	public JsonObject getJsonObject(string key) throws JsonException {
		return getJsonObject(key.get());
	}

	public JsonObject getJsonObject(String key) throws JsonException {
		Object object = get(key);
		if(object instanceof JsonObject)
			return (JsonObject)object;
		throw new JsonException("JSONObject[" + quote(key) + "] is not a JSONObject.");
	}

	public long getLong(string key) throws JsonException {
		return getLong(key.get());
	}

	public long getLong(String key) throws JsonException {
		Object object = get(key);
		try {
			return object instanceof Number ? ((Number)object).longValue() : Long.parseLong((String)object);
		} catch(Exception e) {
			throw new JsonException("JSONObject[" + quote(key) + "] is not a long.");
		}
	}

	public Set<String> getNames() {
		return keySet();
	}

	public String getString(string key) throws JsonException {
		return getString(key.get());
	}

	public String getString(String key) throws JsonException {
		Object object = get(key);
		return object == null ? null : object.toString();
	}

	public boolean has(string key) {
		return has(key.get());
	}

	public boolean has(String key) {
		return super.containsKey(key);
	}

	public int length() {
		return super.size();
	}

	public static String numberToString(Number number) throws JsonException {
		String string = number.toString();
		if(string.indexOf('.') > 0 && string.indexOf('e') < 0 && string.indexOf('E') < 0) {
			while(string.endsWith("0"))
				string = string.substring(0, string.length() - 1);

			if(string.endsWith("."))
				string = string.substring(0, string.length() - 1);
		}
		return string;
	}

	public JsonObject put(String key, Collection<?> value) throws JsonException {
		super.put(key, (Object)(value instanceof JsonArray ? value : new JsonArray(value)));
		return this;
	}

	public JsonObject put(string key, Collection<?> value) throws JsonException {
		return put(key.get(), value);
	}

	public JsonObject put(String key, Map<?, ?> value) throws JsonException {
		put(key, (Object)(value instanceof JsonObject ? value : new JsonObject(value)));
		return this;
	}

	public JsonObject put(string key, Map<?, ?> value) throws JsonException {
		return put(key.get(), value);
	}

	@Override
	public JsonObject put(String key, Object value) throws JsonException {
		if(value != null)
			super.put(key, wrap(value));
		else
			remove(key);
		return this;
	}

	public JsonObject put(string key, Object value) throws JsonException {
		return put(key.get(), value);
	}

	public JsonObject putAll(JsonObject obj) {
		this.putAll((Map<String, Object>)obj);
		return this;
	}

	public static String quote(String string) {
		if(string == null || string.length() == 0)
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

	public String toString(int indentFactor) throws JsonException {
		return toString(indentFactor, 0);
	}

	String toString(int indentFactor, int indent) throws JsonException {
		int i;
		int length = this.length();
		if(length == 0)
			return "{}";

		Iterator<String> keys = keySet().iterator();
		int newindent = indent + indentFactor;
		Object object;
		StringBuilder sb = new StringBuilder(1024).append("{");
		if(length == 1) {
			object = keys.next();
			sb.append(quote(object.toString()));
			sb.append(": ");
			sb.append(valueToString(super.get(object), indentFactor, indent));
		} else {
			while(keys.hasNext()) {
				object = keys.next();
				if(sb.length() > 1)
					sb.append(",\n");
				else
					sb.append('\n');

				for(i = 0; i < newindent; i += 1)
					sb.append(' ');

				sb.append(quote(object.toString()));
				sb.append(": ");
				sb.append(valueToString(super.get(object), indentFactor, newindent));
			}
			if(sb.length() > 1) {
				sb.append('\n');
				for(i = 0; i < indent; i += 1)
					sb.append(' ');
			}
		}
		sb.append('}');
		return sb.toString();
	}

	public static String valueToString(Object value) throws JsonException {
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
			return quote(minMax ? "" : value.toString());
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

	static String valueToString(Object value, int indentFactor, int indent) throws JsonException {
		if(value == null)
			return "null";

		try {
			if(value instanceof JsonString) {
				Object o = ((JsonString)value).toJSONString();
				if(o instanceof String)
					return (String)o;
			}
		} catch(Exception ignore) {
		}
		if(value instanceof Number)
			return numberToString((Number)value);

		if(value instanceof Boolean)
			return value.toString();

		if(value instanceof JsonObject)
			return ((JsonObject)value).toString(indentFactor, indent);

		if(value instanceof JsonArray)
			return ((JsonArray)value).toString(indentFactor, indent);

		if(value instanceof Map)
			return new JsonObject((Map<?, ?>)value).toString(indentFactor, indent);

		if(value instanceof Collection)
			return new JsonArray((Collection<?>)value).toString(indentFactor, indent);

		if(value.getClass().isArray())
			return new JsonArray(value).toString(indentFactor, indent);

		return quote(value.toString());
	}

	public static Object wrap(Object object)  throws JsonException {
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

		return null;
	}
}
