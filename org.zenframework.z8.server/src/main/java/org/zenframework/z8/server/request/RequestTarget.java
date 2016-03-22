package org.zenframework.z8.server.request;

import java.util.Map;

import org.zenframework.z8.server.base.model.NamedObject;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.JsonWriter;

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
        JsonWriter writer = new JsonWriter();

        writer.startResponse(id(), true);

        response.setWriter(writer);

        writeResponse(writer);

        IMonitor monitor = request().getMonitor();

        if(monitor != this)
            monitor.writeResponse(writer);

        writer.finishResponse();
        
        response.setContent(writer.toString());
    }
}
