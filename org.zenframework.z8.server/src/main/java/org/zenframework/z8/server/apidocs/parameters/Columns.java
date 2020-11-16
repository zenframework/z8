package org.zenframework.z8.server.apidocs.parameters;

import org.zenframework.z8.server.apidocs.IActionRequest;
import org.zenframework.z8.server.apidocs.IRequestParameter;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonObject;

import java.util.Collections;

public class Columns implements IRequestParameter {

    @Override
    public String getKey() {
        return "columns";
    }

    @Override
    public Object getValue(Query query, IActionRequest action) {
        JsonObject jsonObject = new JsonObject();
        for (Field field : query.getDataFields()) {
            if (field.hasAttribute(Json.apiDescription.toString())){
                jsonObject.put("id", field.index());
                jsonObject.put("width", "");
            }
        }
        return Collections.singletonList(jsonObject);
    }
}
