package org.zenframework.z8.server.apidocs.parameters;

import org.zenframework.z8.server.apidocs.IActionRequest;
import org.zenframework.z8.server.apidocs.IRequestParameter;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.json.Json;

import java.util.ArrayList;
import java.util.List;

public class Fields implements IRequestParameter {

    @Override
    public String getKey() {
        return "fields";
    }

    @Override
    public Object getValue(Query query, IActionRequest action) {
        List<String> fields = new ArrayList<>();
        for (Field field : query.getDataFields()) {
            if (field.hasAttribute(Json.apiDescription.toString())) {
                fields.add(field.index());
            }
        }
        return fields;
    }
}
