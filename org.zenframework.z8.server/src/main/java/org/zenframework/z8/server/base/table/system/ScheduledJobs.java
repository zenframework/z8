package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.job.scheduler.Scheduler;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.Cron;

public class ScheduledJobs extends Table {
	static public String TableName = "SystemScheduledJobs";
	static public String DefaultCron = "0 * * * *";
	static public int MinRepeat = 10;

	static public class fieldNames {
		public final static String Jobs = "Jobs";
		public final static String Users = "Users";
		public final static String Job = "Job";
		public final static String User = "User";
		public final static String Cron = "Cron";
		public final static String Active = "Active";
		public final static String LastStart = "LastStart";
		public final static String NextStart = "NextStart";
	}

	static public class strings {
		public final static String Title = "ScheduledJobs.title";
		public final static String Settings = "ScheduledJobs.settings";
		public final static String Cron = "ScheduledJobs.cron";
		public final static String Active = "ScheduledJobs.active";
		public final static String LastStart = "ScheduledJobs.lastStart";
		public final static String NextStart = "ScheduledJobs.nextStart";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Settings = Resources.get(strings.Settings);
		public final static String Cron = Resources.get(strings.Cron);
		public final static String Active = Resources.get(strings.Active);
		public final static String LastStart = Resources.get(strings.LastStart);
		public final static String NextStart = Resources.get(strings.NextStart);
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

	public DatetimeField.CLASS<DatetimeField> lastStart = new DatetimeField.CLASS<DatetimeField>(this);
	public DatetimeField.CLASS<DatetimeField> nextStart = new DatetimeField.CLASS<DatetimeField>(this);
	public StringField.CLASS<StringField> cron = new StringField.CLASS<StringField>(this);
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
		objects.add(cron);
		objects.add(lastStart);
		objects.add(nextStart);
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

		cron.setName(fieldNames.Cron);
		cron.setIndex("cron");
		cron.setDisplayName(displayNames.Cron);

		lastStart.setName(fieldNames.LastStart);
		lastStart.setIndex("lastStart");
		lastStart.setDisplayName(displayNames.LastStart);

		nextStart.setName(fieldNames.NextStart);
		nextStart.setIndex("nextStart");
		nextStart.setDisplayName(displayNames.NextStart);

		active.setName(fieldNames.Active);
		active.setIndex("active");
		active.setDisplayName(displayNames.Active);

		cron.get().setDefault(new string(DefaultCron));
		active.get().setDefault(bool.True);
	}

	@Override
	public void afterCreate(guid recordId, guid parentId) {
		super.afterCreate(recordId, parentId);
		Scheduler.reset();
	}

	@Override
	public void beforeUpdate(guid recordId) {
		super.beforeUpdate(recordId);
		if (cron.get().changed()) {
			String cronExp = cron.get().string().get();
			if (cronExp.isEmpty())
				active.get().set(bool.False);
			else if (!Cron.checkExp(cronExp))
				throw new exception("CRON value must be a valid CRON expression:"
						+ "\n    +---------- minute (0 - 59)"
						+ "\n    | +-------- hour (0 - 23)"
						+ "\n    | | +------ day of month (1 - 31)"
						+ "\n    | | | +---- month (1 - 12)"
						+ "\n    | | | | +-- day of week (0 - 6)"
						+ "\n    | | | | |"
						+ "\n    X X X X X - CRON expression contains 5 fields"
						+ "\n"
						+ "\n Value of a field can be:"
						+ "\n    - asterisk '*' means any value"
						+ "\n    - number, i.e. '1', '2', etc."
						+ "\n    - period, i.e. '1-5', '2-23'"
						+ "\n    - asterisk or period with multiplicity, i.e. '*/2' (any even value), '2-9/3' (any value multiple of 3 between 2 and 9)"
						+ "\n    - comma-separated list of other values, i.e. '1,2-4,6-12/3,*/5'"
						+ "\n"
						+ "\n Examples:"
						+ "\n    * * * * * - every minute"
						+ "\n    */5 * * * * - every five minutes"
						+ "\n    5 0 * * * - every day at 00:05"
						+ "\n    15 14 1 * * - every 1st day of month at 14:15"
						+ "\n   0 22 * * 1-5 - Monday to Friday at 22:00"
						+ "\n    23 */2 * * * - at 23rd minute of every even hour"
						+ "\n    15 10,13 * * 1,4 - Monday and Thursday at 10:15 and 13:15");
		}
	}

	@Override
	public void afterUpdate(guid recordId) {
		super.afterUpdate(recordId);
		if (!lastStart.get().changed() && !nextStart.get().changed())
			Scheduler.reset();
	}

	@Override
	public void afterDestroy(guid recordId) {
		super.afterDestroy(recordId);
		Scheduler.reset();
	}
}
