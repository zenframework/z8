package org.zenframework.z8.server.apidocs.parameters;

import org.zenframework.z8.server.apidocs.IActionRequest;
import org.zenframework.z8.server.apidocs.IRequestParameter;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;

public class Data implements IRequestParameter {

    @Override
    public String getKey() {
        return "data";
    }

    @Override
    public Object getValue(Query query, IActionRequest action) {
        JsonObject jsonObject = new JsonObject();
        for (Field field : query.getDataFields()) {
            if (field.hasAttribute(Json.apiDescription.toString())) {
                jsonObject.put(field.id(), field.getDefault());
            }
        }
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(jsonObject);
        return jsonArray;
    }
}
