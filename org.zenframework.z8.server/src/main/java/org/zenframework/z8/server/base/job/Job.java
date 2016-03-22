package org.zenframework.z8.server.base.job;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.model.command.IParameter;
import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.request.RequestTarget;
import org.zenframework.z8.server.types.guid;

public class Job extends RequestTarget {
    private static Map<String, JobMonitor> monitors = Collections.synchronizedMap(new HashMap<String, JobMonitor>());

    private Procedure procedure;
    private Thread thread;
    private JobMonitor monitor;
    private boolean isDone = false;

    static public JobMonitor getMonitor(String id) {
        return monitors.get(id);
    }

    static public JobMonitor createMonitor(Job job) {
        JobMonitor monitor = new JobMonitor(job, guid.create().toString());
        ApplicationServer.getRequest().setMonitor(monitor);
        monitors.put(monitor.id(), monitor);
        return monitor;
    }

    static public void removeMonitor(JobMonitor monitor) {
        monitors.remove(monitor.id());
    }

    public Job(Procedure procedure) {
        super(procedure.classId());
        this.procedure = procedure;

        monitor = createMonitor(this);
    }

    public Thread getThread() {
        return thread;
    }
    
    public boolean scheduled() {
        return getParameters().get(Json.scheduled) != null;
    }

    @Override
    public void writeResponse(JsonWriter writer) {
        setParameters();

        if(!scheduled()) {
            thread = new Thread(procedure, procedure.displayName());
            thread.start();
    
            writer.writeProperty(Json.jobId, monitor.id());
            writer.writeProperty(Json.serverId, ApplicationServer.get().id());
            writer.writeProperty(Json.text, procedure.displayName());
        } else {
            procedure.run();
            isDone = true;
            removeMonitor(monitor);
        }
    }

    private void setParameters() {
        String data = getParameters().get(Json.parameters);

        if(data == null) {
            return;
        }

        JsonObject object = new JsonObject(data);

        if(object != null) {
            String[] names = JsonObject.getNames(object);

            if(names != null) {
                for(String parameterId : names) {
                    IParameter parameter = procedure.getParameter(parameterId);
                    String value = object.getString(parameterId);
                    parameter.parse(value);
                }
            }
        }
    }

    public boolean isDone() {
        return scheduled() ? isDone : !thread.isAlive();
    }
}
