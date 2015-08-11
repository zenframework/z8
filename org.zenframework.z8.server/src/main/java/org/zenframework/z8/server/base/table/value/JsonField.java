package org.zenframework.z8.server.base.table.value;

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

    public org.zenframework.z8.server.json.parser.JsonObject getJson() {
        return new org.zenframework.z8.server.json.parser.JsonObject(get().toString());
    }

    public void set(org.zenframework.z8.server.json.parser.JsonObject json) {
        set(json.toString());        
    }

    public JsonObject.CLASS<JsonObject> z8_getJson() {
        JsonObject.CLASS<JsonObject> jsonObject = new JsonObject.CLASS<JsonObject>(null);
        jsonObject.get().set(getJson());
        return jsonObject;
    }
    
    @SuppressWarnings("unchecked")
    public TextField.CLASS<? extends TextField> operatorAssign(JsonObject.CLASS<? extends JsonObject> value) {
        set(value.get().getInternalObject());
        return (TextField.CLASS<? extends TextField>) this.getCLASS();
    }

}
