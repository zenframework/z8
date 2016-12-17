package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.job.scheduler.Scheduler;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;

public class SchedulerJobs extends Table {
	static public String TableName = "SystemTasks";
	static public int MinRepeat = 10;
	static public int DefaultRepeat = 3600;

	static public class names {
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
		public final static String Title = "SchedulerJobs.title";
		public final static String Settings = "SchedulerJobs.settings";
		public final static String From = "SchedulerJobs.from";
		public final static String Till = "SchedulerJobs.till";
		public final static String Repeat = "SchedulerJobs.repeat";
		public final static String Active = "SchedulerJobs.active";
		public final static String LastStarted = "SchedulerJobs.lastStarted";
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
			setJavaClass(SchedulerJobs.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new SchedulerJobs(container);
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

	public SchedulerJobs(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		jobs.setIndex("jobs");

		users.setIndex("users");

		description.setDisplayName(displayNames.Settings);

		job.setName(names.Job);
		job.setIndex("job");

		user.setName(names.User);
		user.setIndex("user");
		user.get().setDefault(Users.System);

		from.setName(names.From);
		from.setIndex("from");
		from.setDisplayName(displayNames.From);

		till.setName(names.Till);
		till.setIndex("till");
		till.setDisplayName(displayNames.Till);

		repeat.setName(names.Repeat);
		repeat.setIndex("repeat");
		repeat.setDisplayName(displayNames.Repeat);

		lastStarted.setName(names.LastStarted);
		lastStarted.setIndex("lastStarted");
		lastStarted.setDisplayName(displayNames.LastStarted);

		active.setName(names.Active);
		active.setIndex("active");
		active.setDisplayName(displayNames.Active);

		job.get().operatorAssign(jobs);
		user.get().operatorAssign(users);

		registerDataField(job);
		registerDataField(user);
		registerDataField(from);
		registerDataField(till);
		registerDataField(repeat);
		registerDataField(lastStarted);
		registerDataField(active);

		repeat.get().setDefault(new integer(DefaultRepeat));
		active.get().setDefault(bool.True);

		registerFormField(jobs.get().name);
		registerFormField(users.get().name);
		registerFormField(description);
		registerFormField(from);
		registerFormField(till);
		registerFormField(repeat);
		registerFormField(lastStarted);
		registerFormField(active);

		queries.add(jobs);
		queries.add(users);
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
