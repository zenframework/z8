package org.zenframework.z8.server.base.job.scheduler;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.AttachmentField;
import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;

public class TaskLogs extends Table {
    final static public String TableName = "SystemTaskLogs";

    static public class names {
        public final static String Started = "Started";
        public final static String Finished = "Finished";
        public final static String Task = "Task";
        public final static String Files = "Files";
    }

    static public class strings {
        public final static String Title = "TaskLogs.title";
        public final static String Started = "TaskLogs.started";
        public final static String Finished = "TaskLogs.finished";
    }

    public static class CLASS<T extends TaskLogs> extends Table.CLASS<T> {
        public CLASS() {
            this(null);
        }

        public CLASS(IObject container) {
            super(container);
            setJavaClass(TaskLogs.class);
            setName(TableName);
            setDisplayName(Resources.get(TaskLogs.strings.Title));
        }

        @Override
        public Object newObject(IObject container) {
            return new TaskLogs(container);
        }
    }

    public Tasks.CLASS<Tasks> tasks = new Tasks.CLASS<Tasks>(this);

    public Link.CLASS<Link> task = new Link.CLASS<Link>(this);

    public DatetimeField.CLASS<DatetimeField> started = new DatetimeField.CLASS<DatetimeField>(this);
    public DatetimeField.CLASS<DatetimeField> finished = new DatetimeField.CLASS<DatetimeField>(this);

    public AttachmentField.CLASS<AttachmentField> files = new AttachmentField.CLASS<AttachmentField>(this);
    
    public TaskLogs(IObject container) {
        super(container);
    }

    @Override
    public void constructor2() {
        super.constructor2();

        model = tasks;

        tasks.setIndex("tasks");
        
        task.setName(names.Task);
        task.setIndex("task");

        started.setName(names.Started);
        started.setIndex("started");
        started.setDisplayName(Resources.get(strings.Started));

        finished.setName(names.Finished);
        finished.setIndex("finished");
        finished.setDisplayName(Resources.get(strings.Finished));

        files.setName(names.Files);
        files.setIndex("files");

        task.get().operatorAssign(tasks);

        registerDataField(task);
        registerDataField(started);
        registerDataField(finished);
        registerDataField(files);

        registerFormField(started);
        registerFormField(finished);

        registerFormField(tasks.get().jobs.get().name);
        registerFormField(tasks.get().users.get().name);
        registerFormField(tasks.get().from);
        registerFormField(tasks.get().till);
        registerFormField(tasks.get().repeat);
        registerFormField(tasks.get().lastStarted);
        registerFormField(tasks.get().active);

        queries.add(tasks);

        links.add(task);
    }
    
    @Override
    public AttachmentField attachmentField() {
        return files.get();
    }
    
}
