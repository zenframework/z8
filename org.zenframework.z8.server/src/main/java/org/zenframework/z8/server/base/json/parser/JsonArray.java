package org.zenframework.z8.server.base.json.parser;

import org.zenframework.z8.server.json.parser.JsonUtils;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class JsonArray extends OBJECT {

	public static class CLASS<T extends JsonArray> extends OBJECT.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(JsonArray.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new JsonArray(container);
		}
	}

	private org.zenframework.z8.server.json.parser.JsonArray array = new org.zenframework.z8.server.json.parser.JsonArray();

	public JsonArray(IObject container) {
		super(container);
	}

	public org.zenframework.z8.server.json.parser.JsonArray get() {
		return array;
	}

	public void set(org.zenframework.z8.server.json.parser.JsonArray array) {
		this.array = array;
	}

	public void operatorAssign(string source) {
		array = source.isEmpty() ? new org.zenframework.z8.server.json.parser.JsonArray() : new org.zenframework.z8.server.json.parser.JsonArray(source.get());
	}

	@SuppressWarnings("rawtypes")
	public void operatorAssign(RCollection values) {
		array = new org.zenframework.z8.server.json.parser.JsonArray(values);
	}

	public bool z8_isEmpty() {
		return new bool(array.length() == 0);
	}

	public integer z8_length() {
		return new integer(array.length());
	}

	public string z8_getString(integer i) {
		return new string(array.getString(i.getInt()));
	}

	public integer z8_getInt(integer i) {
		return new integer(array.getInt(i.getInt()));
	}

	public decimal z8_getDecimal(integer i) {
		return new decimal(array.getDouble(i.getInt()));
	}

	public bool z8_getBool(integer i) {
		return new bool(array.getBoolean(i.getInt()));
	}

	public guid z8_getGuid(integer i) {
		return array.getGuid(i.getInt());
	}

	public JsonArray.CLASS<? extends JsonArray> z8_getJsonArray(integer i) {
		JsonArray.CLASS<? extends JsonArray> cls = new JsonArray.CLASS<JsonArray>(null);
		cls.get().set(array.getJsonArray(i.getInt()));
		return cls;
	}

	public JsonObject.CLASS<? extends JsonObject> z8_getJsonObject(integer i) {
		JsonObject.CLASS<? extends JsonObject> cls = new JsonObject.CLASS<JsonObject>(null);
		cls.get().set(array.getJsonObject(i.getInt()));
		return cls;
	}

	@SuppressWarnings("unchecked")
	public JsonArray.CLASS<? extends JsonArray> z8_put(primary value) {
		array.put(JsonUtils.unwrap(value));
		return (JsonArray.CLASS<? extends JsonArray>)getCLASS();
	}

	@SuppressWarnings("unchecked")
	public JsonArray.CLASS<? extends JsonArray> z8_put(JsonArray.CLASS<? extends JsonArray> value) {
		array.put(value.get().get());
		return (JsonArray.CLASS<? extends JsonArray>)getCLASS();
	}

	@SuppressWarnings("unchecked")
	public JsonArray.CLASS<? extends JsonArray> z8_put(JsonObject.CLASS<? extends JsonObject> value) {
		array.put(value.get().getInternalObject());
		return (JsonArray.CLASS<? extends JsonArray>)getCLASS();
	}

	@SuppressWarnings("unchecked")
	public JsonArray.CLASS<? extends JsonArray> z8_put(integer index, primary value) {
		array.put(index.getInt(), JsonUtils.unwrap(value));
		return (JsonArray.CLASS<? extends JsonArray>)getCLASS();
	}

	@SuppressWarnings("unchecked")
	public JsonArray.CLASS<? extends JsonArray> z8_insert(integer index, JsonObject.CLASS<? extends JsonObject> value) {
		array.insert(index.getInt(), value.get().getInternalObject());
		return (JsonArray.CLASS<? extends JsonArray>)getCLASS();
	}

	@SuppressWarnings("unchecked")
	public JsonArray.CLASS<? extends JsonArray> z8_put(integer index, JsonArray.CLASS<? extends JsonArray> value) {
		array.put(index.getInt(), value.get().get());
		return (JsonArray.CLASS<? extends JsonArray>)getCLASS();
	}

	@SuppressWarnings("unchecked")
	public JsonArray.CLASS<? extends JsonArray> z8_put(integer index, JsonObject.CLASS<? extends JsonObject> value) {
		array.put(index.getInt(), value.get().getInternalObject());
		return (JsonArray.CLASS<? extends JsonArray>)getCLASS();
	}

	@SuppressWarnings("unchecked")
	public JsonArray.CLASS<? extends JsonArray> z8_remove(integer index) {
		array.remove(index.getInt());
		return (JsonArray.CLASS<? extends JsonArray>)getCLASS();
	}

	public RCollection<guid> z8_toGuidArray() {
		RCollection<guid> result = new RCollection<guid>();
		for(int i = 0; i < array.size(); i++) {
			result.add(array.getGuid(i));
		}
		return result;
	}

	public RCollection<string> z8_toStringArray() {
		RCollection<string> result = new RCollection<string>();
		for(int i = 0; i < array.size(); i++) {
			result.add(new string(array.getString(i)));
		}
		return result;
	}

	@Override
	public String toString() {
		return array.toString();
	}

	@Override
	public string z8_toString() {
		return new string(array.toString());
	}

	public string z8_toString(integer indentFactor) {
		return new string(array.toString(indentFactor.getInt()));
	}

	public static JsonArray.CLASS<JsonArray> z8_parse(string source) {
		JsonArray.CLASS<JsonArray> jsonArray = new JsonArray.CLASS<JsonArray>(null);
		jsonArray.get().set(new org.zenframework.z8.server.json.parser.JsonArray(source.get()));
		return jsonArray;
	}
}
