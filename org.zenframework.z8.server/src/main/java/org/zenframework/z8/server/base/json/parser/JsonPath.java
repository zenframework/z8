package org.zenframework.z8.server.base.json.parser;

import org.zenframework.z8.server.json.parser.JsonUtils;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class JsonPath extends OBJECT {

	public static class CLASS<T extends JsonPath> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(JsonPath.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new JsonPath(container);
		}
	}

	public JsonPath(IObject container) {
		super(container);
	}

	private org.zenframework.z8.server.json.parser.JsonPath path;

	public org.zenframework.z8.server.json.parser.JsonPath get() {
		return path;
	}

	public void set(String path) {
		this.path = new org.zenframework.z8.server.json.parser.JsonPath(path);
	}

	public JsonPath.CLASS<? extends JsonPath> operatorAssign(string value) {
		set(value.get());
		return (JsonPath.CLASS<?>)getCLASS();
	}

	public integer z8_length() {
		return new integer(path.length());
	}

	public string z8_name(integer part) {
		return new string(path.name(part.getInt()));
	}

	public JsonPath.CLASS<? extends JsonPath> z8_getParent() {
		JsonPath.CLASS<JsonPath> jsonPath = new JsonPath.CLASS<JsonPath>(null);
		jsonPath.get().set(path.parent().toString());
		return jsonPath;
	}

	public JsonArray.CLASS<? extends JsonArray> z8_getJsonArray(Object json) {
		return (JsonArray.CLASS<?>)JsonUtils.wrap(path.evaluate(json));
	}

	public JsonObject.CLASS<? extends JsonObject> z8_getJsonObject(Object json) {
		return (JsonObject.CLASS<?>)JsonUtils.wrap(path.evaluate(json));
	}

	public string z8_getString(Object json) {
		return (string)JsonUtils.wrap(path.evaluate(json));
	}

	public integer z8_getInt(Object json) {
		return (integer)JsonUtils.wrap(path.evaluate(json));
	}

	public decimal z8_getDecimal(Object json) {
		return (decimal)JsonUtils.wrap(path.evaluate(json));
	}

	public bool z8_getBool(Object json) {
		return (bool)JsonUtils.wrap(path.evaluate(json));
	}

	public guid z8_getGuid(Object json) {
		return (guid)JsonUtils.wrap(path.evaluate(json));
	}

	public date z8_getDate(Object json) {
		return (date)JsonUtils.wrap(path.evaluate(json));
	}

	public JsonArray.CLASS<? extends JsonArray> z8_get(Object json, JsonArray.CLASS<? extends JsonArray> defaultValue) {
		return (JsonArray.CLASS<?>)JsonUtils.wrap(path.evaluate(json, defaultValue));
	}

	public JsonObject.CLASS<? extends JsonObject> z8_get(Object json, JsonObject.CLASS<? extends JsonObject> defaultValue) {
		return (JsonObject.CLASS<?>)JsonUtils.wrap(path.evaluate(json, defaultValue));
	}

	public string z8_get(Object json, string defaultValue) {
		return (string)JsonUtils.wrap(path.evaluate(json, defaultValue));
	}

	public integer z8_get(Object json, integer defaultValue) {
		return (integer)JsonUtils.wrap(path.evaluate(json, defaultValue));
	}

	public decimal z8_get(Object json, decimal defaultValue) {
		return (decimal)JsonUtils.wrap(path.evaluate(json, defaultValue));
	}

	public bool z8_get(Object json, bool defaultValue) {
		return (bool)JsonUtils.wrap(path.evaluate(json, defaultValue));
	}

	public guid z8_get(Object json, guid defaultValue) {
		return (guid)JsonUtils.wrap(path.evaluate(json, defaultValue));
	}

	public date z8_get(Object json, date defaultValue) {
		return (date)JsonUtils.wrap(path.evaluate(json, defaultValue));
	}

	@Override
	public string z8_toString() {
		return new string(path.toString());
	}

	public static JsonPath.CLASS<JsonPath> z8_path(string path) {
		JsonPath.CLASS<JsonPath> jsonPath = new JsonPath.CLASS<JsonPath>(null);
		jsonPath.get().set(path.get());
		return jsonPath;
	}

}
