package org.zenframework.z8.server.base.json.parser;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.exception;
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
        if (value instanceof bool) {
            array.put(((bool) value).get());
        } else if (value instanceof date || value instanceof datetime) {
            array.put(((date) value).get().toString());
        } else if (value instanceof decimal) {
            array.put(((decimal) value).get());
        } else if (value instanceof guid) {
            array.put(((guid) value).toString());
        } else if (value instanceof integer) {
            array.put(((integer) value).get());
        } else if (value instanceof string) {
            array.put(((string) value).get());
        } else {
            throw new exception("Unsupported primary type " + value.getClass().getSimpleName());
        }
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
        if (value instanceof bool) {
            array.put(index.getInt(), ((bool) value).get());
        } else if (value instanceof date || value instanceof datetime) {
            array.put(index.getInt(), ((date) value).get().toString());
        } else if (value instanceof decimal) {
            array.put(index.getInt(), ((decimal) value).get());
        } else if (value instanceof guid) {
            array.put(index.getInt(), ((guid) value).toString());
        } else if (value instanceof integer) {
            array.put(index.getInt(), ((integer) value).get());
        } else if (value instanceof string) {
            array.put(index.getInt(), ((string) value).get());
        } else {
            throw new exception("Unsupported primary type " + value.getClass().getSimpleName());
        }
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

    @Override
    public String toString() {
        return array.toString();
    }

    @Override
    public string z8_toString() {
        return new string(array.toString());
    }

    public static JsonArray.CLASS<JsonArray> getJsonArray(org.zenframework.z8.server.json.parser.JsonArray json) {
        JsonArray.CLASS<JsonArray> jsonArray = new JsonArray.CLASS<JsonArray>(null);
        jsonArray.get().set(json);
        return jsonArray;
    }

    public static JsonArray.CLASS<JsonArray> z8_newJsonArray(string source) {
        return getJsonArray(new org.zenframework.z8.server.json.parser.JsonArray(source.get()));
    }

}
