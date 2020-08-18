package org.zenframework.z8.server.apidocs.request_parameters;

import com.google.gson.internal.LinkedTreeMap;
import org.zenframework.z8.server.apidocs.IActionRequest;
import org.zenframework.z8.server.apidocs.IRequestParametr;
import org.zenframework.z8.server.base.query.Query;

import java.util.Collections;
import java.util.Map;

public class Columns implements IRequestParametr {

    @Override
    public String getKey() {
        return "columns";
    }

    @Override
    public Object getValue(Query query, IActionRequest action) {
        Map<String, Object> params = new LinkedTreeMap<>();
        query.getDataFields()
                .stream()
                .filter(field -> field.hasAttribute("APIDescription"))
                .forEach(field -> {
                    params.put("id", field.index());
                    params.put("width", "");});

        return Collections.singletonList(params);
    }
}
