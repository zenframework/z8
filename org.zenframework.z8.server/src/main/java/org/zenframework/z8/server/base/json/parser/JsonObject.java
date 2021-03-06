package org.zenframework.z8.server.base.json.parser;

import java.util.Map;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.json.parser.JsonUtils;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class JsonObject extends OBJECT {

	public static class CLASS<T extends JsonObject> extends OBJECT.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(JsonObject.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new JsonObject(container);
		}
	}

	private org.zenframework.z8.server.json.parser.JsonObject object = new org.zenframework.z8.server.json.parser.JsonObject();

	public JsonObject(IObject container) {
		super(container);
	}

	public JsonObject(IObject container, org.zenframework.z8.server.json.parser.JsonObject jsonObject) {
		super(container);
		set(jsonObject);
	}

	public void set(org.zenframework.z8.server.json.parser.JsonObject object) {
		this.object = object;
	}

	public string string() {
		return new string(toString());
	}

	public void operatorAssign(string source) {
		object = source.isEmpty() ? new org.zenframework.z8.server.json.parser.JsonObject() : new org.zenframework.z8.server.json.parser.JsonObject(source.get());
	}

	public org.zenframework.z8.server.json.parser.JsonObject getInternalObject() {
		return object;
	}

	public bool z8_isEmpty() {
		return new bool(object.length() == 0);
	}

	public integer z8_length() {
		return new integer(object.length());
	}

	public RCollection<string> z8_getNames() {
		RCollection<string> result = new RCollection<string>();

		String[] names = org.zenframework.z8.server.json.parser.JsonObject.getNames(object);

		for(String name : names) {
			result.add(new string(name));
		}

		return result;
	}

	public bool z8_has(string name) {
		return new bool(object.has(name));
	}

	public string z8_getString(string name) {
		return new string(object.getString(name));
	}

	public integer z8_getInt(string name) {
		return new integer(object.getInt(name));
	}

	public decimal z8_getDecimal(string name) {
		return new decimal(object.getDouble(name));
	}

	public bool z8_getBool(string name) {
		return new bool(object.getBoolean(name));
	}

	public guid z8_getGuid(string name) {
		return object.getGuid(name);
	}

	public date z8_getDate(string name) {
		return date.z8_parse(z8_getString(name));
	}

	public JsonArray.CLASS<? extends JsonArray> z8_getJsonArray(string name) {
		org.zenframework.z8.server.json.parser.JsonArray array = this.object.getJsonArray(name);
		JsonArray.CLASS<? extends JsonArray> cls = new JsonArray.CLASS<JsonArray>(null);
		cls.get().set(array);
		return cls;
	}

	public JsonObject.CLASS<? extends JsonObject> z8_getJsonObject(string name) {
		org.zenframework.z8.server.json.parser.JsonObject object = this.object.getJsonObject(name);
		JsonObject.CLASS<? extends JsonObject> cls = new JsonObject.CLASS<JsonObject>(null);
		cls.get().set(object);
		return cls;
	}

	@SuppressWarnings("unchecked")
	public JsonObject.CLASS<? extends JsonObject> z8_put(string name, primary value) {
		object.put(name, JsonUtils.unwrap(value));
		return (JsonObject.CLASS<? extends JsonObject>)getCLASS();
	}

	@SuppressWarnings("unchecked")
	public JsonObject.CLASS<? extends JsonObject> z8_put(string name, JsonArray.CLASS<? extends JsonArray> value) {
		object.put(name.get(), value.get().get());
		return (JsonObject.CLASS<? extends JsonObject>)getCLASS();
	}

	@SuppressWarnings("unchecked")
	public JsonObject.CLASS<? extends JsonObject> z8_put(string name, JsonObject.CLASS<? extends JsonObject> value) {
		object.put(name.get(), value.get().getInternalObject());
		return (JsonObject.CLASS<? extends JsonObject>)getCLASS();
	}

	public JsonObject.CLASS<? extends JsonObject> z8_put(Field.CLASS<? extends Field> fieldClass) {
		Field field = fieldClass.get();
		return z8_put(field.z8_id(), field.get());
	}

	@SuppressWarnings("unchecked")
	public JsonObject.CLASS<? extends JsonObject> z8_remove(string name) {
		object.remove(name.get());
		return (JsonObject.CLASS<? extends JsonObject>)getCLASS();
	}

	@Override
	public String toString() {
		return object.toString();
	}

	@Override
	public string z8_toString() {
		return new string(object.toString());
	}

	public string z8_toString(integer indentFactor) {
		return new string(object.toString(indentFactor.getInt()));
	}

	public static JsonObject.CLASS<JsonObject> getJsonObject(org.zenframework.z8.server.json.parser.JsonObject json) {
		JsonObject.CLASS<JsonObject> jsonObject = new JsonObject.CLASS<JsonObject>(null);
		jsonObject.get().set(json);
		return jsonObject;
	}

	public static JsonObject.CLASS<JsonObject> z8_parse(string source) {
		return getJsonObject(new org.zenframework.z8.server.json.parser.JsonObject(source.get()));
	}

	@SuppressWarnings("rawtypes")
	public static JsonObject.CLASS<? extends JsonObject> z8_fromPrimaryMap(RLinkedHashMap map) {
		JsonObject.CLASS<? extends JsonObject> jsonObject = new JsonObject.CLASS<JsonObject>();
		jsonObject.get().set(new org.zenframework.z8.server.json.parser.JsonObject(map));
		return jsonObject;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static JsonObject.CLASS<? extends JsonObject> z8_fromJsonArrayMap(RLinkedHashMap map) {
		RLinkedHashMap<primary, JsonArray.CLASS<? extends JsonArray>> jsonArrayMap = (RLinkedHashMap<primary, JsonArray.CLASS<? extends JsonArray>>)map;
		JsonObject.CLASS<? extends JsonObject> jsonObject = new JsonObject.CLASS<JsonObject>();
		for(Map.Entry<primary, JsonArray.CLASS<? extends JsonArray>> ja : jsonArrayMap.entrySet()) {
			jsonObject.get().getInternalObject().put(ja.getKey().toString(), ja.getValue().get().get());
		}
		return jsonObject;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static JsonObject.CLASS<? extends JsonObject> z8_fromJsonObjectMap(RLinkedHashMap map) {
		RLinkedHashMap<primary, JsonObject.CLASS<? extends JsonObject>> jsonObjectMap = (RLinkedHashMap<primary, JsonObject.CLASS<? extends JsonObject>>)map;
		JsonObject.CLASS<? extends JsonObject> jsonObject = new JsonObject.CLASS<JsonObject>();
		for(Map.Entry<primary, JsonObject.CLASS<? extends JsonObject>> jo : jsonObjectMap.entrySet()) {
			jsonObject.get().getInternalObject().put(jo.getKey().toString(), jo.getValue().get().getInternalObject());
		}
		return jsonObject;
	}
}
