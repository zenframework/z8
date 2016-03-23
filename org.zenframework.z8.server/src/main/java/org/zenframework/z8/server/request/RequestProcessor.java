package org.zenframework.z8.server.request;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.guid;

public class RequestProcessor {
    class RequestInfo {
        Thread thread;
        IRequest request;
        IResponse response;
    }

    private static Map<String, RequestInfo> requests = Collections.synchronizedMap(new HashMap<String, RequestInfo>());

    private boolean isThreadAlive(Thread thread) {
        try {
            return thread.isAlive();
        }
        catch(Throwable e) {
            Trace.logError(e);
            return false;
        }
    }

    public void processRequest(IRequest request, IResponse response) {
        String id = request.getParameter(Json.retry);

        RequestInfo info;

        if(id == null) {
            RequestDispatcher dispatcher = new RequestDispatcher(request, response);

            info = new RequestInfo();
            info.thread = new Thread(dispatcher, request.getParameter(Json.ip));
            info.request = request;
            info.response = response;

            id = guid.create().toString(false);

            requests.put(id, info);

            info.thread.start();
        } else
            info = requests.get(id);

        if(isThreadAlive(info.thread)) {
            try {
                info.thread.join(10 * 1000);
            }
            catch(InterruptedException e) {
                System.out.println(info.thread.getName() + " has been interrupted");
            }
        }

        boolean finished = !isThreadAlive(info.thread);

        if(!finished) {
            JsonObject writer = new JsonObject();
            writer.put(Json.requestId, id);
            writer.put(Json.success, true);
            writer.put(Json.type, "event");
            writer.put(Json.retry, id);
            writer.put(Json.serverId, ApplicationServer.Id());
            response.setContent(writer.toString());
        }
        else {
            requests.remove(id);

            if(response != info.response) {
                response.setContent(info.response.getContent());
            }
        }
    }
}
