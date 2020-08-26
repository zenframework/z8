package org.zenframework.z8.server.apidocs.request_parameters;

import com.google.gson.internal.LinkedTreeMap;
import org.zenframework.z8.server.apidocs.IActionRequest;
import org.zenframework.z8.server.apidocs.IRequestParametr;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.db.sql.expressions.Operation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Filter implements IRequestParametr {

    @Override
    public String getKey() {
        return "filter";
    }

    @Override
    public Object getValue(Query query, IActionRequest action) {
        List<Map<String, String>> filterParamExample = Collections.singletonList(new LinkedTreeMap<>());
        filterParamExample.get(0).put("property", query.primaryKey().index());
        filterParamExample.get(0).put("operator", Operation.Eq.toString());
        filterParamExample.get(0).put("value", "00000000-0000-0000-0000-000000000000");

        List<Map<String, Object>> filterExample = Collections.singletonList(new LinkedTreeMap<>());
        filterExample.get(0).put("logical", "and");
        filterExample.get(0).put("expressions", filterParamExample);
        return filterExample;
    }
}
