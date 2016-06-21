package org.zenframework.z8.server.base.job.scheduler;

import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.request.IMonitor;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class Task {

    public guid id;

    public String jobId;
    public String login;
    public String name;
    public String settings;
    public datetime from = new datetime();
    public datetime till = new datetime().addDay(30);
    public int repeat = 1;
    public boolean active = true;

    public datetime lastStarted = new datetime();

    public boolean isRunning = false;
    
    private int executionCount = 0;

    public Task(guid id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return name + "-" + (executionCount + 1);
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof Task) {
            Task task = (Task)object;
            return id.equals(task.id);
        }
        return false;
    }

    public boolean readyToStart() {
        long now = new datetime().getTicks();

        return active && !isRunning && from.getTicks() < now && (till.equals(datetime.MIN) || now < till.getTicks())
                && (lastStarted.equals(datetime.MIN) || lastStarted.addSecond(repeat).getTicks() < now);
    }

    public void start() throws Throwable {
        isRunning = true;
        executionCount++;

        datetime lastStarted = new datetime();

        Tasks tasks = new Tasks.CLASS<Tasks>(null).get();
        tasks.lastStarted.get().set(lastStarted);
        tasks.update(id);

        this.lastStarted = lastStarted;
    }

    public void stop(IMonitor monitor) {
        try {
            file logFile = monitor != null ? monitor.getLog() : null;
    
            if (logFile != null) {
                // [{"id":"ext-comp-2215","size":64000,"time":"05/09/2013 02:52:49","file":"T13_0742.xls","name":"storage\\SystemTasks\\7DBC9271-9B49-453F-A84B-B41F10353627\\E98923B5D6044D80870447FC232F9DE0.xls"}]
                JsonArray writer = new JsonArray();
                JsonObject obj = new JsonObject();
                String fileName = logFile.getRelativePath();
                obj.put(Json.size, 0);
                obj.put(Json.time, new datetime());
                obj.put(Json.name, "log.txt");
                obj.put(Json.path, fileName);
                writer.put(obj);
                
                String attachments = writer.toString();
                TaskLogs logs = new TaskLogs.CLASS<TaskLogs>(null).get();
                logs.task.get().set(id);
                logs.files.get().set(new string(attachments));
                logs.started.get().set(lastStarted);
                logs.finished.get().set(new datetime());
                logs.create();
            }
        } finally {
            isRunning = false;
        }
    }
}
