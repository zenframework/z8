package org.zenframework.z8.server.json.parser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.zenframework.z8.server.types.guid;

public class JsonArray extends ArrayList<Object> {
	private static final long serialVersionUID = -1023358096617596153L;

	public JsonArray() {
	}

	public JsonArray(JsonTokener x) throws JsonException {
		this();
		if(x.nextClean() != '[') {
			throw x.syntaxError("A JSONArray text must start with '['");
		}
		if(x.nextClean() != ']') {
			x.back();
			for(;;) {
				if(x.nextClean() == ',') {
					x.back();
					super.add(null);
				} else {
					x.back();
					super.add(x.nextValue());
				}
				switch(x.nextClean()) {
				case ';':
				case ',':
					if(x.nextClean() == ']') {
						return;
					}
					x.back();
					break;
				case ']':
					return;
				default:
					throw x.syntaxError("Expected a ',' or ']'");
				}
			}
		}
	}

	public JsonArray(String source) throws JsonException {
		this(new JsonTokener(source == null || source.isEmpty() ? "[]" : (source.charAt(0) != '[' ? '[' + source + ']' : source)));
	}

	public JsonArray(Collection<?> collection) {
		if(collection == null)
			return;

		for(Object object : collection)
			super.add(JsonObject.wrap(object));
	}

	public JsonArray(Object array) throws JsonException {
		if(array == null)
			return;

		if(!array.getClass().isArray())
			throw new JsonException("JSONArray initial value should be a string or collection or array.");

		int length = Array.getLength(array);
		for(int i = 0; i < length; i++)
			this.put(JsonObject.wrap(Array.get(array, i)));
	}

	public boolean getBoolean(int index) throws JsonException {
		Object object = get(index);
		if(object.equals(Boolean.FALSE))
			return false;
		if(object.equals(Boolean.TRUE))
			return true;
		throw new JsonException("JSONArray[" + index + "] is not a boolean.");
	}

	public double getDouble(int index) throws JsonException {
		Object object = get(index);
		try {
			return object instanceof Number ? ((Number)object).doubleValue() : Double.parseDouble((String)object);
		} catch(Exception e) {
			throw new JsonException("JSONArray[" + index + "] is not a number.");
		}
	}

	public int getInt(int index) throws JsonException {
		Object object = get(index);
		try {
			return object instanceof Number ? ((Number)object).intValue() : Integer.parseInt((String)object);
		} catch(Exception e) {
			throw new JsonException("JSONArray[" + index + "] is not a number.");
		}
	}

	public guid getGuid(int index) throws JsonException {
		Object object = get(index);
		return object instanceof guid ? (guid)object : new guid(object.toString());
	}

	public JsonArray getJsonArray(int index) throws JsonException {
		Object object = get(index);
		if(object instanceof JsonArray) {
			return (JsonArray)object;
		}
		throw new JsonException("JSONArray[" + index + "] is not a JSONArray.");
	}

	public JsonObject getJsonObject(int index) throws JsonException {
		Object object = get(index);
		if(object instanceof JsonObject) {
			return (JsonObject)object;
		}
		throw new JsonException("JSONArray[" + index + "] is not a JSONObject.");
	}

	public long getLong(int index) throws JsonException {
		Object object = get(index);
		try {
			return object instanceof Number ? ((Number)object).longValue() : Long.parseLong((String)object);
		} catch(Exception e) {
			throw new JsonException("JSONArray[" + index + "] is not a number.");
		}
	}

	public String getString(int index) throws JsonException {
		Object object = get(index);
		return object == null ? null : object.toString();
	}

	public boolean isNull(int index) {
		return get(index) == null;
	}

	public String join(String separator) throws JsonException {
		int len = length();
		StringBuilder sb = new StringBuilder(1024);

		for(int i = 0; i < len; i += 1) {
			if(i > 0) {
				sb.append(separator);
			}
			sb.append(JsonObject.valueToString(super.get(i)));
		}
		return sb.toString();
	}

	public int length() {
		return super.size();
	}

	public JsonArray put(Collection<Object> value) {
		return put((Object)(value instanceof JsonArray ? value : new JsonArray(value)));
	}

	public JsonArray put(Map<?, ?> value) {
		return put((Object)(value instanceof JsonObject ? value : new JsonObject(value)));
	}

	public JsonArray put(Object value) {
		super.add(JsonObject.wrap(value));
		return this;
	}

	public JsonArray put(int index, Collection<Object> value) throws JsonException {
		return put(index, (Object)(value instanceof JsonArray ? value : new JsonArray(value)));
	}

	public JsonArray put(int index, Map<?, ?> value) throws JsonException {
		return put(index, (Object)(value instanceof JsonObject ? value : new JsonObject(value)));
	}

	public JsonArray put(int index, Object value) {
		set(index, JsonObject.wrap(value));
		return this;
	}

	public JsonArray insert(int index, Object value) {
		super.add(index, JsonObject.wrap(value));
		return this;
	}

	@Override
	public String toString() {
		try {
			return '[' + join(",") + ']';
		} catch(Exception e) {
			return null;
		}
	}

	public String toString(int indentFactor) throws JsonException {
		return toString(indentFactor, 0);
	}

	String toString(int indentFactor, int indent) throws JsonException {
		int len = length();
		if(len == 0) {
			return "[]";
		}
		int i;
		StringBuilder sb = new StringBuilder(1024).append("[");
		if(len == 1) {
			sb.append(JsonObject.valueToString(super.get(0), indentFactor, indent));
		} else {
			int newindent = indent + indentFactor;
			sb.append('\n');
			for(i = 0; i < len; i += 1) {
				if(i > 0) {
					sb.append(",\n");
				}
				for(int j = 0; j < newindent; j += 1) {
					sb.append(' ');
				}
				sb.append(JsonObject.valueToString(super.get(i), indentFactor, newindent));
			}
			sb.append('\n');
			for(i = 0; i < indent; i += 1) {
				sb.append(' ');
			}
		}
		sb.append(']');
		return sb.toString();
	}
}
