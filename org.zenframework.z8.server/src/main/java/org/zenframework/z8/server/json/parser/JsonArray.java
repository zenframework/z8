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
		if (array == null)
			return;

		if (!array.getClass().isArray())
			throw new RuntimeException("JSONArray initial value should be a string or collection or array.");

		int length = Array.getLength(array);

		for (int i = 0; i < length; i++)
			this.add(JsonObject.wrap(Array.get(array, i)));
	}

	public boolean isNull(int index) {
		return get(index) == null;
	}

	public bool getBool(int index) {
		return JsonObject.asBool(get(index));
	}

	public boolean getBoolean(int index) {
		return JsonObject.asBoolean(get(index));
	}

	public date getDate(int index) {
		return JsonObject.asDate(get(index));
	}

	public decimal getDecimal(int index) {
		return JsonObject.asDecimal(get(index));
	}

	public double getDouble(int index) {
		return JsonObject.asDouble(get(index));
	}

	public guid getGuid(int index) {
		return JsonObject.asGuid(get(index));
	}

	public integer getInteger(int index) {
		return JsonObject.asInteger(get(index));
	}

	public int getInt(int index) {
		return JsonObject.asInt(get(index));
	}

	public long getLong(int index) {
		return JsonObject.asLong(get(index));
	}

	public String getString(int index) {
		return JsonObject.asString(get(index));
	}

	public JsonArray getJsonArray(int index) {
		return JsonObject.asJsonArray(get(index));
	}

	public JsonObject getJsonObject(int index) {
		return JsonObject.asJsonObject(get(index));
	}

	public String join(String separator) {
		StringBuilder sb = new StringBuilder(1024);

		for (int i = 0; i < size(); i += 1)
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
