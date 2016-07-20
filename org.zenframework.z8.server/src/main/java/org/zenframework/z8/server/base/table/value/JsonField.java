package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.base.json.parser.JsonArray;
import org.zenframework.z8.server.base.json.parser.JsonObject;
import org.zenframework.z8.server.runtime.IObject;

public class JsonField extends TextField {

    public static class CLASS<T extends JsonField> extends TextField.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(JsonField.class);
            setAttribute("native", JsonField.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new JsonField(container);
        }
    }

    public JsonField(IObject container) {
        super(container);
    }

    public org.zenframework.z8.server.json.parser.JsonObject getJsonObject() {
        String value = get().toString();
        return value.isEmpty() ? new org.zenframework.z8.server.json.parser.JsonObject()
                : new org.zenframework.z8.server.json.parser.JsonObject(get().toString());
    }

    public org.zenframework.z8.server.json.parser.JsonArray getJsonArray() {
        String value = get().toString();
        return value.isEmpty() ? new org.zenframework.z8.server.json.parser.JsonArray()
                : new org.zenframework.z8.server.json.parser.JsonArray(get().toString());
    }

    public JsonObject.CLASS<? extends JsonObject> z8_getJsonObject() {
        JsonObject.CLASS<JsonObject> json = new JsonObject.CLASS<JsonObject>(null);
        json.get().set(getJsonObject());
        return json;
    }

    public JsonArray.CLASS<? extends JsonArray> z8_getJsonArray() {
        JsonArray.CLASS<JsonArray> json = new JsonArray.CLASS<JsonArray>(null);
        json.get().set(getJsonArray());
        return json;
    }

    @SuppressWarnings("unchecked")
    public TextField.CLASS<? extends TextField> operatorAssign(JsonObject.CLASS<? extends JsonObject> value) {
        set(value.get().getInternalObject().isEmpty() ? "" : value.get().getInternalObject().toString());
        return (TextField.CLASS<? extends TextField>) this.getCLASS();
    }

    @SuppressWarnings("unchecked")
    public TextField.CLASS<? extends TextField> operatorAssign(JsonArray.CLASS<? extends JsonArray> value) {
        set(value.get().get().isEmpty() ? "" : value.get().get().toString());
        return (TextField.CLASS<? extends TextField>) this.getCLASS();
    }

}
