package org.zenframework.z8.server.apidocs.request_parameters;

import org.zenframework.z8.server.apidocs.IActionRequest;
import org.zenframework.z8.server.apidocs.IRequestParameter;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.json.parser.JsonObject;

import java.util.Collections;

public class Sort implements IRequestParameter {

    @Override
    public String getKey() {
        return "sort";
    }

    @Override
    public Object getValue(Query query, IActionRequest action) {
        JsonObject sortJsonObject = new JsonObject();
        sortJsonObject.put("property", query.primaryKey().index());
        sortJsonObject.put("direction", "asc");
        return Collections.singletonList(sortJsonObject);
    }
}
