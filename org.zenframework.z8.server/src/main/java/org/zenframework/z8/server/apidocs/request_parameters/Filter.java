package org.zenframework.z8.server.apidocs.request_parameters;

import org.zenframework.z8.server.apidocs.IActionRequest;
import org.zenframework.z8.server.apidocs.IRequestParameter;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.json.parser.JsonObject;

import java.util.Collections;

public class Filter implements IRequestParameter {

    @Override
    public String getKey() {
        return "filter";
    }

    @Override
    public Object getValue(Query query, IActionRequest action) {
        JsonObject expressionJsonObject = new JsonObject();
        expressionJsonObject.put("property", query.primaryKey().index());
        expressionJsonObject.put("operator", Operation.Eq.toString());
        expressionJsonObject.put("value", "00000000-0000-0000-0000-000000000000");

        JsonObject filterJsonObject = new JsonObject();
        filterJsonObject.put("logical", "and");
        filterJsonObject.put("expressions", Collections.singletonList(expressionJsonObject));
        return Collections.singletonList(filterJsonObject);
    }
}
