package org.zenframework.z8.server.apidocs.request_parameters;

import org.zenframework.z8.server.apidocs.IActionRequest;
import org.zenframework.z8.server.apidocs.IRequestParameter;
import org.zenframework.z8.server.base.query.Query;

public class Info implements IRequestParameter {

    @Override
    public String getKey() {
        return "info";
    }

    @Override
    public Object getValue(Query query, IActionRequest action) {
        return "{}";
    }
}
