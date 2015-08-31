package org.zenframework.z8.server.base.job.scheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.types.guid;

public class Scheduler implements Runnable {

    private static volatile Scheduler scheduler = null;

    private Thread thread = null;
    private boolean resetPending = true;
    private List<Task> tasks = new ArrayList<Task>();

    public static void start() {
        if (scheduler == null) {
            scheduler = new Scheduler();
        }
    }

    public static void stop() {
        if (scheduler != null) {
            scheduler.thread.interrupt();
            scheduler = null;
        }
    }

    public static void reset() {
        if (scheduler != null)
            scheduler.resetPending = true;
    }

    private Scheduler() {
        thread = new Thread(this, "Z8 scheduler");
        thread.start();
    }

    @Override
    public void run() {
        while (scheduler != null) {
            initializeTasks();

            for (Task task : tasks) {
                if (task.readyToStart())
                    startJob(task);
            }

            try {
                Thread.sleep(1 * 1000);
            } catch (InterruptedException e) {}
        }
    }

    private synchronized void initializeTasks() {
        if(!resetPending)
            return;
        
        Tasks tasksTable = new Tasks.CLASS<Tasks>(null).get();

        Collection<Field> fields = new ArrayList<Field>();

        DatetimeField from = tasksTable.from.get();
        DatetimeField till = tasksTable.till.get();
        IntegerField repeat = tasksTable.repeat.get();
        DatetimeField lastStarted = tasksTable.lastStarted.get();
        BoolField active = tasksTable.active.get();
        StringField jobId = tasksTable.jobs.get().id.get();
        StringField jobName = tasksTable.jobs.get().name.get();
        StringField login = tasksTable.users.get().name.get();

        fields.add(from);
        fields.add(till);
        fields.add(repeat);
        fields.add(lastStarted);
        fields.add(active);
        fields.add(jobId);
        fields.add(jobName);
        fields.add(login);

        try {
            if (ApplicationServer.defaultDatabase().isSystemInstalled()) {
                tasksTable.read(fields);
            } else {
                return;
            }
        } catch (Throwable e) {
            throw new RuntimeException("Can't read tasks", e);
        }

        List<Task> result = new ArrayList<Task>();
        
        while (tasksTable.next()) {
            guid taskId = tasksTable.recordId();
            Task task = new Task(taskId);
            
            int index = tasks.indexOf(task);
            if(index != -1)
                task = tasks.get(index);
            
            task.jobId = jobId.get().string().get();
            task.name = jobName.get().string().get();
            task.login = login.get().string().get();
            task.from = from.get().datetime();
            task.till = till.get().datetime();
            task.lastStarted = lastStarted.get().datetime();
            task.repeat = repeat.get().integer().getInt();
            task.active = active.get().bool().get();

            result.add(task);
        }
        
        tasks = result;

        resetPending = false;
    }

    private ScheduledJob startJob(Task task) {
        ScheduledJob job = new ScheduledJob(task);
        Thread thread = new Thread(job, task.toString());
        thread.start();

        return job;
    }
    
}
