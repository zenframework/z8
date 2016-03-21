package org.zenframework.z8.server.base.job;

import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
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
    public void writeResponse(JsonWriter writer) {
        synchronized(mutex) {
            boolean isDone = isDone();

            writer.writeProperty(Json.jobId, id());

            writer.writeProperty(Json.done, isDone);
            writer.writeProperty(Json.totalWork, totalWork);
            writer.writeProperty(Json.worked, worked);

            writer.writeProperty(Json.serverId, ApplicationServer.Id);
            writer.writeInfo(getMessages(), isDone ? getLog() : null);

            collectLogMessages();
            clearMessages();

            if(isDone)
                Job.removeMonitor(this);
        }
    }
}
