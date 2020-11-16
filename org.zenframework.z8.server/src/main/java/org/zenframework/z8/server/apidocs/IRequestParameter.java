package org.zenframework.z8.server.apidocs;

import org.zenframework.z8.server.base.query.Query;

public interface IRequestParameter {
    String getKey();
    Object getValue(Query query, IActionRequest action);
}
