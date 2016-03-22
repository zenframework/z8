package org.zenframework.z8.server.base.job;

import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.request.Monitor;

public class JobMonitor extends Monitor {
    private Job job;
    private int totalWork = 100;
    private int worked = 0;

    private Object mutex = new Object();

    public JobMonitor(Job job, String id) {
        super(id);
        this.job = job;
    }

    public Job getJob() {
        return job;
    }
    
    public int getTotalWork() {
        return totalWork;
    }

    public void setTotalWork(int totalWork) {
        this.totalWork = totalWork;
    }

    public int getWorked() {
        return worked;
    }

    public void setWorked(int worked) {
        this.worked = worked;
    }

    @Override
    public void print(String text) {
        synchronized(mutex) {
            super.print(text);

            if(job != null && job.scheduled()) {
                log(text);
            }
        }
    }

    public void logMessages() {
        synchronized(mutex) {
            collectLogMessages();
        }
    }

    public boolean isDone() {
        return job != null ? job.isDone() : true;
    }

    @Override
    public void writeResponse(JsonObject writer) {
        synchronized(mutex) {
            boolean isDone = isDone();

            writer.put(Json.jobId, id());

            writer.put(Json.done, isDone);
            writer.put(Json.totalWork, totalWork);
            writer.put(Json.worked, worked);

            writer.put(Json.serverId, ApplicationServer.get().id());
            writer.writeInfo(getMessages(), isDone ? getLog() : null);

            collectLogMessages();
            clearMessages();

            if(isDone) {
                Job.removeMonitor(this);
            }
        }
    }
}
