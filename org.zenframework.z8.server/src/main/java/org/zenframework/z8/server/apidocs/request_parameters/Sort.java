package org.zenframework.z8.server.apidocs.request_parameters;

import com.google.gson.internal.LinkedTreeMap;
import org.zenframework.z8.server.apidocs.IActionRequest;
import org.zenframework.z8.server.apidocs.IRequestParametr;
import org.zenframework.z8.server.base.query.Query;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Sort implements IRequestParametr {

    @Override
    public String getKey() {
        return "sort";
    }

    @Override
    public Object getValue(Query query, IActionRequest action) {
        List<Map<String, String>> sortParamExample = Collections.singletonList(new LinkedTreeMap<>());
        sortParamExample.get(0).put("property", query.primaryKey().index());
        sortParamExample.get(0).put("direction", "asc");
        return sortParamExample;
    }
}
