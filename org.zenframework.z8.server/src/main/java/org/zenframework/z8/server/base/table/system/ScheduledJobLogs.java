package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.AttachmentField;
import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;

public class ScheduledJobLogs extends Table {
	final static public String TableName = "SystemScheduledJobLogs";

	static public class names {
		public final static String Start = "Start";
		public final static String Finished = "Finish";
		public final static String ScheduledJob = "ScheduledJob";
		public final static String Files = "Files";
	}

	static public class strings {
		public final static String Title = "ScheduledJobLogs.title";
		public final static String Start = "ScheduledJobLogs.start";
		public final static String Finish = "ScheduledJobLogs.finish";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Start = Resources.get(strings.Start);
		public final static String Finish = Resources.get(strings.Finish);
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

	public AttachmentField.CLASS<AttachmentField> files = new AttachmentField.CLASS<AttachmentField>(this);

	public ScheduledJobLogs(IObject container) {
		super(container);
	}

	@Override
	public void constructor1() {
		scheduledJob.get(IClass.Constructor1).operatorAssign(scheduledJobs);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		scheduledJobs.setIndex("scheduledJobs");

		scheduledJob.setName(names.ScheduledJob);
		scheduledJob.setIndex("scheduledJob");

		start.setName(names.Start);
		start.setIndex("start");
		start.setDisplayName(displayNames.Start);

		finish.setName(names.Finished);
		finish.setIndex("finish");
		finish.setDisplayName(displayNames.Finish);

		files.setName(names.Files);
		files.setIndex("files");

		registerDataField(scheduledJob);
		registerDataField(start);
		registerDataField(finish);
		registerDataField(files);

		objects.add(scheduledJobs);
	}
}
