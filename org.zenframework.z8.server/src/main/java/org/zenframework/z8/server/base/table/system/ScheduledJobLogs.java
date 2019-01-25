package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.FileField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;

public class ScheduledJobLogs extends Table {
	final static public String TableName = "SystemScheduledJobLogs";

	static public class fieldNames {
		public final static String Start = "Start";
		public final static String Finished = "Finish";
		public final static String ScheduledJob = "ScheduledJob";
		public final static String Files = "Files";
	}

	static public class strings {
		public final static String Title = "ScheduledJobLogs.title";
		public final static String Description = "ScheduledJobLogs.description";
		public final static String Start = "ScheduledJobLogs.start";
		public final static String Finish = "ScheduledJobLogs.finish";
		public final static String File = "ScheduledJobLogs.file";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Description = Resources.get(strings.Description);
		public final static String Start = Resources.get(strings.Start);
		public final static String Finish = Resources.get(strings.Finish);
		public final static String File = Resources.get(strings.File);
	}

	public static class CLASS<T extends ScheduledJobLogs> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(ScheduledJobLogs.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new ScheduledJobLogs(container);
		}
	}

	static public ScheduledJobLogs newInstance() {
		return new ScheduledJobLogs.CLASS<ScheduledJobLogs>(null).get();
	}

	public ScheduledJobs.CLASS<ScheduledJobs> scheduledJobs = new ScheduledJobs.CLASS<ScheduledJobs>(this);

	public Link.CLASS<Link> scheduledJob = new Link.CLASS<Link>(this);

	public DatetimeField.CLASS<DatetimeField> start = new DatetimeField.CLASS<DatetimeField>(this);
	public DatetimeField.CLASS<DatetimeField> finish = new DatetimeField.CLASS<DatetimeField>(this);

	public FileField.CLASS<FileField> file = new FileField.CLASS<FileField>(this);

	public ScheduledJobLogs(IObject container) {
		super(container);
	}

	@Override
	public void constructor1() {
		scheduledJob.get(IClass.Constructor1).operatorAssign(scheduledJobs);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(scheduledJob);
		objects.add(start);
		objects.add(finish);
		objects.add(file);

		objects.add(scheduledJobs);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		description.setDisplayName(displayNames.Description);

		scheduledJobs.setIndex("scheduledJobs");

		scheduledJob.setName(fieldNames.ScheduledJob);
		scheduledJob.setIndex("scheduledJob");

		start.setName(fieldNames.Start);
		start.setIndex("start");
		start.setDisplayName(displayNames.Start);

		finish.setName(fieldNames.Finished);
		finish.setIndex("finish");
		finish.setDisplayName(displayNames.Finish);

		file.setName(fieldNames.Files);
		file.setIndex("file");
		file.setDisplayName(displayNames.File);
	}
}
