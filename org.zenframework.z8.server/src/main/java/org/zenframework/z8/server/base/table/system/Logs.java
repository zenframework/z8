package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.AttachmentField;
import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;

public class Logs extends Table {
	final static public String TableName = "SystemTaskLogs";

	static public class names {
		public final static String Started = "Started";
		public final static String Finished = "Finished";
		public final static String Job = "Task";
		public final static String Files = "Files";
	}

	static public class strings {
		public final static String Title = "Logs.title";
		public final static String Started = "Logs.started";
		public final static String Finished = "Logs.finished";
	}

	public static class CLASS<T extends Logs> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Logs.class);
			setName(TableName);
			setDisplayName(Resources.get(Logs.strings.Title));
		}

		@Override
		public Object newObject(IObject container) {
			return new Logs(container);
		}
	}

	static public Logs newInstance() {
		return new Logs.CLASS<Logs>(null).get();
	}

	public SchedulerJobs.CLASS<SchedulerJobs> jobs = new SchedulerJobs.CLASS<SchedulerJobs>(this);

	public Link.CLASS<Link> job = new Link.CLASS<Link>(this);

	public DatetimeField.CLASS<DatetimeField> started = new DatetimeField.CLASS<DatetimeField>(this);
	public DatetimeField.CLASS<DatetimeField> finished = new DatetimeField.CLASS<DatetimeField>(this);

	public AttachmentField.CLASS<AttachmentField> files = new AttachmentField.CLASS<AttachmentField>(this);

	public Logs(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		jobs.setIndex("jobs");

		job.setName(names.Job);
		job.setIndex("job");

		started.setName(names.Started);
		started.setIndex("started");
		started.setDisplayName(Resources.get(strings.Started));

		finished.setName(names.Finished);
		finished.setIndex("finished");
		finished.setDisplayName(Resources.get(strings.Finished));

		files.setName(names.Files);
		files.setIndex("files");

		job.get().operatorAssign(jobs);

		registerDataField(job);
		registerDataField(started);
		registerDataField(finished);
		registerDataField(files);

		registerFormField(started);
		registerFormField(finished);

		registerFormField(jobs.get().jobs.get().name);
		registerFormField(jobs.get().users.get().name);
		registerFormField(jobs.get().description);
		registerFormField(jobs.get().from);
		registerFormField(jobs.get().till);
		registerFormField(jobs.get().repeat);
		registerFormField(jobs.get().lastStarted);
		registerFormField(jobs.get().active);

		queries.add(jobs);

		links.add(job);
	}
}
