package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.form.Listbox;
import org.zenframework.z8.server.base.query.Query;
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

	public Listbox.CLASS<Listbox> logs = new Listbox.CLASS<Listbox>(this);

	public JobsView(IObject container) {
		super(container);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void constructor2() {
		super.constructor2();

		columnCount = new integer(12);

		readOnly = new bool(!ApplicationServer.getUser().isAdministrator());

		logs.setIndex("logs");
		logs.setDisplayName(ScheduledJobLogs.displayNames.Title);

		ScheduledJobLogs logsTable = new ScheduledJobLogs.CLASS<ScheduledJobLogs>(this).get();

		logs.get().query = (Query.CLASS<Query>)logsTable.getCLASS();
		logs.get().link = logsTable.scheduledJob;
		logs.get().readOnly = bool.True;
		logs.get().colspan = new integer(12);
		logs.get().flex = new integer(1);

		logsTable.columns.add(logsTable.start);
		logsTable.columns.add(logsTable.finish);
		logsTable.sortFields.add(logsTable.start);
		logsTable.start.get().sortDirection = SortDirection.Desc;

		jobs.get().name.get().colspan = new integer(4);
		from.get().colspan = new integer(2);
		till.get().colspan = new integer(2);
		repeat.get().colspan = new integer(2);
		active.get().colspan = new integer(2);

		registerControl(jobs.get().name);
		registerControl(from);
		registerControl(till);
		registerControl(repeat);
		registerControl(active);
		registerControl(logs);

		sortFields.add(jobs.get().name);
	}
}
