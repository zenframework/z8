package org.zenframework.z8.server.request;

import java.util.Map;

import org.zenframework.z8.server.base.model.NamedObject;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonObject;

public abstract class RequestTarget extends NamedObject implements IRequestTarget {
    static public IRequest request() {
        return ApplicationServer.getRequest();
    }

    static public Map<String, String> getParameters() {
        return request().getParameters();
    }

    public RequestTarget(String id) {
        super(id);
    }

    @Override
    public void processRequest(IResponse response) throws Throwable {
        JsonObject writer = new JsonObject();

        writer.put(Json.requestId, id());
        writer.put(Json.success, true);
        writer.put(Json.type, "event");

        response.setWriter(writer);

        writeResponse(writer);

        IMonitor monitor = request().getMonitor();

        if(monitor != this)
            monitor.writeResponse(writer);

        response.setContent(writer.toString());
    }
}
