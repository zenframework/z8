package org.zenframework.z8.server.apidocs;

import org.zenframework.z8.server.base.query.Query;

public interface IActionRequest {
    String getName();
    String getDescription();
    void makeExample(Query query);

    String getRequest();
    String getResponse();
}
