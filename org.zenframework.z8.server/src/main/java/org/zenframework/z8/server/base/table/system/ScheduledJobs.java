package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.job.scheduler.Scheduler;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;

public class ScheduledJobs extends Table {
	static public String TableName = "SystemScheduledJobs";
	static public int MinRepeat = 10;
	static public int DefaultRepeat = 3600;

	static public class fieldNames {
		public final static String Jobs = "Jobs";
		public final static String Users = "Users";
		public final static String Job = "Job";
		public final static String User = "User";
		public final static String From = "From";
		public final static String Till = "Till";
		public final static String Repeat = "Repeat";
		public final static String Active = "Active";
		public final static String LastStarted = "LastStarted";
	}

	static public class strings {
		public final static String Title = "ScheduledJobs.title";
		public final static String Settings = "ScheduledJobs.settings";
		public final static String From = "ScheduledJobs.from";
		public final static String Till = "ScheduledJobs.till";
		public final static String Repeat = "ScheduledJobs.repeat";
		public final static String Active = "ScheduledJobs.active";
		public final static String LastStarted = "ScheduledJobs.lastStarted";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Settings = Resources.get(strings.Settings);
		public final static String From = Resources.get(strings.From);
		public final static String Till = Resources.get(strings.Till);
		public final static String Repeat = Resources.get(strings.Repeat);
		public final static String Active = Resources.get(strings.Active);
		public final static String LastStarted = Resources.get(strings.LastStarted);
	}

	public static class CLASS<T extends Table> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(ScheduledJobs.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new ScheduledJobs(container);
		}
	}

	public Jobs.CLASS<Jobs> jobs = new Jobs.CLASS<Jobs>(this);
	public Users.CLASS<Users> users = new Users.CLASS<Users>(this);

	public Link.CLASS<Link> job = new Link.CLASS<Link>(this);
	public Link.CLASS<Link> user = new Link.CLASS<Link>(this);

	public DatetimeField.CLASS<DatetimeField> from = new DatetimeField.CLASS<DatetimeField>(this);
	public DatetimeField.CLASS<DatetimeField> till = new DatetimeField.CLASS<DatetimeField>(this);
	public DatetimeField.CLASS<DatetimeField> lastStarted = new DatetimeField.CLASS<DatetimeField>(this);
	public IntegerField.CLASS<IntegerField> repeat = new IntegerField.CLASS<IntegerField>(this);
	public BoolField.CLASS<BoolField> active = new BoolField.CLASS<BoolField>(this);

	public ScheduledJobs(IObject container) {
		super(container);
	}

	@Override
	public void constructor1() {
		job.get(IClass.Constructor1).operatorAssign(jobs);
		user.get(IClass.Constructor1).operatorAssign(users);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(job);
		objects.add(user);
		objects.add(from);
		objects.add(till);
		objects.add(repeat);
		objects.add(lastStarted);
		objects.add(active);

		objects.add(jobs);
		objects.add(users);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		jobs.setIndex("jobs");

		users.setIndex("users");

		description.setDisplayName(displayNames.Settings);

		job.setName(fieldNames.Job);
		job.setIndex("job");

		user.setName(fieldNames.User);
		user.setIndex("user");
		user.get().setDefault(Users.System);

		from.setName(fieldNames.From);
		from.setIndex("from");
		from.setDisplayName(displayNames.From);

		till.setName(fieldNames.Till);
		till.setIndex("till");
		till.setDisplayName(displayNames.Till);

		repeat.setName(fieldNames.Repeat);
		repeat.setIndex("repeat");
		repeat.setDisplayName(displayNames.Repeat);

		lastStarted.setName(fieldNames.LastStarted);
		lastStarted.setIndex("lastStarted");
		lastStarted.setDisplayName(displayNames.LastStarted);

		active.setName(fieldNames.Active);
		active.setIndex("active");
		active.setDisplayName(displayNames.Active);

		repeat.get().setDefault(new integer(DefaultRepeat));
		active.get().setDefault(bool.True);
	}

	@Override
	public void afterCreate(guid recordId, guid parentId) {
		super.afterCreate(recordId, parentId);
		Scheduler.reset();
	}

	@Override
	public void afterUpdate(guid recordId) {
		super.afterUpdate(recordId);
		if (!lastStarted.get().changed())
			Scheduler.reset();
	}

	@Override
	public void afterDestroy(guid recordId) {
		super.afterDestroy(recordId);
		Scheduler.reset();
	}
}
