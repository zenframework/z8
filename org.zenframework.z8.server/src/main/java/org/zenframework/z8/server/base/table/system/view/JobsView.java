package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.form.Listbox;
import org.zenframework.z8.server.base.table.system.ScheduledJobLogs;
import org.zenframework.z8.server.base.table.system.ScheduledJobs;
import org.zenframework.z8.server.db.sql.SortDirection;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class JobsView extends ScheduledJobs {
	static public class strings {
		public final static String Job = "JobsView.job";
		public final static String User = "JobsView.user";
	}

	static public class displayNames {
		public final static String Job = Resources.get(strings.Job);
		public final static String User = Resources.get(strings.User);
	}

	public static class CLASS<T extends JobsView> extends ScheduledJobs.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(JobsView.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new JobsView(container);
		}
	}

	public Listbox.CLASS<Listbox> logsListbox = new Listbox.CLASS<Listbox>(this);
	public ScheduledJobLogs.CLASS<ScheduledJobLogs> logs = new ScheduledJobLogs.CLASS<ScheduledJobLogs>(this);

	public JobsView(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(logs);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		colCount = new integer(12);

		readOnly = new bool(!ApplicationServer.getUser().isAdministrator());

		logs.setIndex("logs");

		logsListbox.setIndex("logsListbox");
		logsListbox.setDisplayName(ScheduledJobLogs.displayNames.Title);

		logsListbox.get().query = logs;
		logsListbox.get().link = logs.get().scheduledJob;
//		logsListbox.get().readOnly = bool.True;
		logsListbox.get().colSpan = new integer(12);
		logsListbox.get().flex = new integer(1);

		logs.get().file.get().editable = bool.True;

		logsListbox.get().columns.add(logs.get().start);
		logsListbox.get().columns.add(logs.get().finish);
		logsListbox.get().columns.add(logs.get().description);
		logsListbox.get().columns.add(logs.get().file);
		logsListbox.get().sortFields.add(logs.get().start);

		logs.get().start.get().sortDirection = SortDirection.Desc;

		nextStart.get().readOnly = bool.True;

		jobs.get().name.get().colSpan = new integer(2);
		users.get().name.get().colSpan = new integer(2);
		cron.get().colSpan = new integer(2);
		active.get().colSpan = new integer(2);
		lastStart.get().colSpan = new integer(2);
		nextStart.get().colSpan = new integer(2);

		jobs.get().name.setDisplayName(displayNames.Job);
		users.get().name.setDisplayName(displayNames.User);

		registerControl(jobs.get().name);
		registerControl(users.get().name);
		registerControl(cron);
		registerControl(active);
		registerControl(lastStart);
		registerControl(nextStart);
		registerControl(logsListbox);

		names.add(jobs.get().name);
		names.add(users.get().name);

		sortFields.add(jobs.get().name);
	}
}
