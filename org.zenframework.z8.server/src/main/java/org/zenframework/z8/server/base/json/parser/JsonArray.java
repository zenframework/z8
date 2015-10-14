package org.zenframework.z8.server.base.json.parser;

import org.apache.commons.codec.binary.Base64;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.binary;
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
            setAttribute(Native, JsonArray.class.getCanonicalName());
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

    public void set(org.zenframework.z8.server.json.parser.JsonArray array) {
        this.array = array;
    }

    public void operatorAssign(string source) {
        array = source.isEmpty() ? new org.zenframework.z8.server.json.parser.JsonArray()
                : new org.zenframework.z8.server.json.parser.JsonArray(source.get());
    }

    public org.zenframework.z8.server.json.parser.JsonArray getInternalArray() {
        return array;
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

    public binary z8_getBinary(integer i) {
        return new binary(Base64.decodeBase64(array.getString(i.getInt())));
    }

    public JsonArray.CLASS<? extends JsonArray> z8_getJsonArray(integer i) {
        org.zenframework.z8.server.json.parser.JsonArray array = this.array.getJsonArray(i.getInt());
        JsonArray.CLASS<? extends JsonArray> cls = new JsonArray.CLASS<JsonArray>(null);
        cls.get().set(array);
        return cls;
    }

    public JsonObject.CLASS<? extends JsonObject> z8_getJsonObject(integer i) {
        org.zenframework.z8.server.json.parser.JsonObject object = this.array.getJsonObject(i.getInt());
        JsonObject.CLASS<? extends JsonObject> cls = new JsonObject.CLASS<JsonObject>(null);
        cls.get().set(object);
        return cls;
    }

    @SuppressWarnings("unchecked")
    public JsonArray.CLASS<? extends JsonArray> z8_put(primary value) {
        array.put(primary.unwrap(value));
        return (JsonArray.CLASS<? extends JsonArray>) getCLASS();
    }

    @SuppressWarnings("unchecked")
    public JsonArray.CLASS<? extends JsonArray> z8_put(JsonArray.CLASS<? extends JsonArray> value) {
        array.put(value.get().getInternalArray());
        return (JsonArray.CLASS<? extends JsonArray>) getCLASS();
    }

    @SuppressWarnings("unchecked")
    public JsonArray.CLASS<? extends JsonArray> z8_put(JsonObject.CLASS<? extends JsonObject> value) {
        array.put(value.get().getInternalObject());
        return (JsonArray.CLASS<? extends JsonArray>) getCLASS();
    }

    @SuppressWarnings("unchecked")
    public JsonArray.CLASS<? extends JsonArray> z8_put(integer index, primary value) {
        array.put(index.getInt(), primary.unwrap(value));
        return (JsonArray.CLASS<? extends JsonArray>) getCLASS();
    }

    @SuppressWarnings("unchecked")
    public JsonArray.CLASS<? extends JsonArray> z8_put(integer index, JsonArray.CLASS<? extends JsonArray> value) {
        array.put(index.getInt(), value.get().getInternalArray());
        return (JsonArray.CLASS<? extends JsonArray>) getCLASS();
    }

    @SuppressWarnings("unchecked")
    public JsonArray.CLASS<? extends JsonArray> z8_put(integer index, JsonObject.CLASS<? extends JsonObject> value) {
        array.put(index.getInt(), value.get().getInternalObject());
        return (JsonArray.CLASS<? extends JsonArray>) getCLASS();
    }

    @SuppressWarnings("unchecked")
    public JsonArray.CLASS<? extends JsonArray> z8_remove(integer index) {
        array.remove(index.getInt());
        return (JsonArray.CLASS<? extends JsonArray>) getCLASS();
    }

    public RCollection<guid> z8_toGuidArray() {
        return new RCollection<guid>(array.toArray(new guid[array.size()]));
    }

    public RCollection<string> z8_toStringArray() {
        return new RCollection<string>(array.toArray(new string[array.size()]));
    }

    public RCollection<JsonArray.CLASS<? extends JsonArray>> z8_toJsonArrayArray() {
        RCollection<JsonArray.CLASS<? extends JsonArray>> jsonArrayArray = new RCollection<JsonArray.CLASS<? extends JsonArray>>();
        for (int i = 0; i < array.size(); i++) {
            JsonArray.CLASS<? extends JsonArray> jsonArray = new JsonArray.CLASS<JsonArray>();
            jsonArray.get().set(array.getJsonArray(i));
            jsonArrayArray.add(jsonArray);
        }
        return jsonArrayArray;
    }

    public RCollection<JsonObject.CLASS<? extends JsonObject>> z8_toJsonObjectArray() {
        RCollection<JsonObject.CLASS<? extends JsonObject>> jsonObjectArray = new RCollection<JsonObject.CLASS<? extends JsonObject>>();
        for (int i = 0; i < array.size(); i++) {
            JsonObject.CLASS<? extends JsonObject> jsonObject = new JsonObject.CLASS<JsonObject>();
            jsonObject.get().set(array.getJsonObject(i));
            jsonObjectArray.add(jsonObject);
        }
        return jsonObjectArray;
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

    public static JsonArray.CLASS<JsonArray> getJsonArray(org.zenframework.z8.server.json.parser.JsonArray json) {
        JsonArray.CLASS<JsonArray> jsonArray = new JsonArray.CLASS<JsonArray>(null);
        jsonArray.get().set(json);
        return jsonArray;
    }

    public static JsonArray.CLASS<JsonArray> z8_parse(string source) {
        return getJsonArray(new org.zenframework.z8.server.json.parser.JsonArray(source.get()));
    }

    @SuppressWarnings("rawtypes")
    public static JsonArray.CLASS<? extends JsonArray> z8_fromPrimaryArray(RCollection array) {
        JsonArray.CLASS<? extends JsonArray> jsonArray = new JsonArray.CLASS<JsonArray>();
        jsonArray.get().set(new org.zenframework.z8.server.json.parser.JsonArray(array));
        return jsonArray;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static JsonArray.CLASS<? extends JsonArray> z8_fromJsonArrayArray(RCollection array) {
        RCollection<JsonArray.CLASS<? extends JsonArray>> jsonArrayArray = (RCollection<JsonArray.CLASS<? extends JsonArray>>) array;
        JsonArray.CLASS<? extends JsonArray> jsonArray = new JsonArray.CLASS<JsonArray>();
        for (JsonArray.CLASS<? extends JsonArray> ja : jsonArrayArray) {
            jsonArray.get().getInternalArray().put(ja.get().getInternalArray());
        }
        return jsonArray;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static JsonArray.CLASS<? extends JsonArray> z8_fromJsonObjectArray(RCollection array) {
        RCollection<JsonObject.CLASS<? extends JsonObject>> jsonObjectArray = (RCollection<JsonObject.CLASS<? extends JsonObject>>) array;
        JsonArray.CLASS<? extends JsonArray> jsonArray = new JsonArray.CLASS<JsonArray>();
        for (JsonObject.CLASS<? extends JsonObject> jo : jsonObjectArray) {
            jsonArray.get().getInternalArray().put(jo.get().getInternalObject());
        }
        return jsonArray;
    }

}
