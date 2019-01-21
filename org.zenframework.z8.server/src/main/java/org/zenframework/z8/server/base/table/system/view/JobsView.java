package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.form.Listbox;
import org.zenframework.z8.server.base.table.system.ScheduledJobLogs;
import org.zenframework.z8.server.base.table.system.ScheduledJobs;
import org.zenframework.z8.server.db.sql.SortDirection;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class JobsView extends ScheduledJobs {
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
		logsListbox.get().readOnly = bool.True;
		logsListbox.get().colSpan = new integer(12);
		logsListbox.get().flex = new integer(1);

		logsListbox.get().columns.add(logs.get().start);
		logsListbox.get().columns.add(logs.get().finish);
		logsListbox.get().columns.add(logs.get().description);
		logsListbox.get().sortFields.add(logs.get().start);

		logs.get().start.get().sortDirection = SortDirection.Desc;

		jobs.get().name.get().colSpan = new integer(4);
		cron.get().colSpan = new integer(2);
		active.get().colSpan = new integer(2);
		lastStart.get().colSpan = new integer(2);
		nextStart.get().colSpan = new integer(2);

		registerControl(jobs.get().name);
		registerControl(cron);
		registerControl(active);
		registerControl(lastStart);
		registerControl(nextStart);
		registerControl(logsListbox);

		sortFields.add(jobs.get().name);
	}
}
