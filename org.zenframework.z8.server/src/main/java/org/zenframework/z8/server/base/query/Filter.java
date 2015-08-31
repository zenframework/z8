package org.zenframework.z8.server.base.query;

import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;

public class Filter extends OBJECT {
    public static class CLASS<T extends Filter> extends OBJECT.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(Filter.class);
            setAttribute(Native, Filter.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new Filter(container);
        }
    }

    public RCollection<GuidField.CLASS<? extends GuidField>> fields = new RCollection<GuidField.CLASS<? extends GuidField>>(
            true);
    public GuidField.CLASS<? extends GuidField> value;

    public Filter(IObject container) {
        super(container);
    }

    @Override
    public void write(JsonObject writer) {
        JsonArray fieldsArr = new JsonArray();
        for(GuidField.CLASS<? extends GuidField> field : fields) {
            fieldsArr.put(field.id());
        }
        writer.put(Json.fields, fieldsArr);
        writer.put(Json.value, value.id());
    }

}
