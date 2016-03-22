package org.zenframework.z8.server.request;

import org.zenframework.z8.server.json.JsonWriter;

public interface IRequestTarget extends INamedObject {
    public void processRequest(IResponse response) throws Throwable;

    public void writeResponse(/*JsonObject*/ JsonWriter writer) throws Throwable;
}
