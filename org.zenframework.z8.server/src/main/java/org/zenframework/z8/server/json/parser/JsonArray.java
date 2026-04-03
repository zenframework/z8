package org.zenframework.z8.server.json.parser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;

public class JsonArray extends ArrayList<Object> {
	private static final long serialVersionUID = -1023358096617596153L;

	public JsonArray() {
	}

	public JsonArray(JsonTokener x) {
		this();
		if(x.nextClean() != '[') {
			x.syntaxError("A JSONArray text must start with '['");
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
					x.syntaxError("Expected a ',' or ']'");
				}
			}
		}
	}

	public JsonArray(String source) {
		this(new JsonTokener(source == null || source.isEmpty() ? "[]" : (source.charAt(0) != '[' ? '[' + source + ']' : source)));
	}

	public JsonArray(Collection<?> collection) {
		if(collection == null)
			return;

		for(Object object : collection)
			super.add(JsonObject.wrap(object));
	}

	public JsonArray(Object array) {
		if(array == null)
			return;

		if(!array.getClass().isArray())
			throw new RuntimeException("JSONArray initial value should be a string or collection or array.");

		int length = Array.getLength(array);
		for(int i = 0; i < length; i++)
			this.add(JsonObject.wrap(Array.get(array, i)));
	}

	public boolean isNull(int index) {
		return get(index) == null;
	}

	public bool getBool(int index) {
		Object object = get(index);

		if(object == null || object instanceof bool)
			return (bool)object;

		return new bool(getBoolean(object));
	}

	public boolean getBoolean(int index) {
		return getBoolean(get(index));
	}

	private boolean getBoolean(Object object) {
		return object.equals(Boolean.TRUE);
	}

	public date getDate(int index) {
		Object object = get(index);

		if(object == null || object instanceof date)
			return (date)object;

		try {
			return new date(getLong(object));
		} catch (Exception e) {
			return date.parse(object.toString());
		}
	}

	public decimal getDecimal(int index) {
		Object object = get(index);

		if(object == null || object instanceof integer)
			return (decimal)object;

		return new decimal(getDouble(object));
	}

	public double getDouble(int index) {
		return getDouble(get(index));
	}

	private double getDouble(Object object) {
		return object instanceof Number ? ((Number)object).doubleValue() : Double.parseDouble((String)object);
	}

	public guid getGuid(int index) {
		Object object = get(index);

		if(object == null || object instanceof guid)
			return (guid)object;

		return new guid(object.toString());
	}

	public integer getInteger(int index) {
		Object object = get(index);

		if(object == null || object instanceof integer)
			return (integer)object;

		return new integer(getLong(object));
	}

	public int getInt(int index) {
		return getInt(get(index));
	}

	private int getInt(Object object) {
		return object instanceof Number ? ((Number)object).intValue() : Integer.parseInt((String)object);
	}

	public long getLong(int index) {
		return getLong(get(index));
	}

	private long getLong(Object object) {
		return object instanceof Number ? ((Number)object).longValue() : Long.parseLong((String)object);
	}

	public String getString(int index) {
		Object object = get(index);
		return object == null ? null : object.toString();
	}

	public JsonArray getJsonArray(int index) {
		Object object = get(index);
		if(object instanceof JsonArray)
			return (JsonArray)object;
		throw new RuntimeException("JSONArray[" + index + "] is not a JSONArray.");
	}

	public JsonObject getJsonObject(int index) {
		Object object = get(index);
		if(object instanceof JsonObject)
			return (JsonObject)object;
		throw new RuntimeException("JSONArray[" + index + "] is not a JSONObject.");
	}

	public String join(String separator) {
		StringBuilder sb = new StringBuilder(1024);

		for(int i = 0; i < size(); i += 1)
			sb.append((i != 0 ? separator : "") + JsonObject.valueToString(super.get(i)));

		return sb.toString();
	}

	public JsonArray add(Collection<Object> value) {
		add((Object)(value instanceof JsonArray ? value : new JsonArray(value)));
		return this;
	}

	public JsonArray add(Map<?, ?> value) {
		add((Object)(value instanceof JsonObject ? value : new JsonObject(value)));
		return this;
	}

	@Override
	public boolean add(Object value) {
		return super.add(JsonObject.wrap(value));
	}

	public JsonArray insert(int index, Object value) {
		super.add(index, JsonObject.wrap(value));
		return this;
	}

	public JsonArray set(int index, Collection<Object> value) {
		return set(index, (Object)(value instanceof JsonArray ? value : new JsonArray(value)));
	}

	public JsonArray set(int index, Map<?, ?> value) {
		return set(index, (Object)(value instanceof JsonObject ? value : new JsonObject(value)));
	}

	public guid[] toGuidArray() {
		Collection<guid> guids = new ArrayList<guid>();
		for(int i = 0; i < size(); i++)
			guids.add(getGuid(i));
		return guids.toArray(new guid[0]);
	}

	@Override
	public JsonArray set(int index, Object value) {
		if(index == size())
			add(value);
		else
			super.set(index, JsonObject.wrap(value));
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
}
