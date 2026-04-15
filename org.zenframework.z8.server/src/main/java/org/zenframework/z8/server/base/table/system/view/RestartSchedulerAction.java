package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.job.scheduler.Scheduler;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.system.ScheduledJobs;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;

public class RestartSchedulerAction extends Action {
	static public class CLASS<T extends RestartSchedulerAction> extends Action.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(RestartSchedulerAction.class);
			setDisplayName(ScheduledJobs.displayNames.RestartScheduler);
		}

		@Override
		public Object newObject(IObject container) {
			return new RestartSchedulerAction(container);
		}
	}

	public RestartSchedulerAction(IObject container) {
		super(container);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_execute(RCollection records, Query.CLASS<? extends Query> context, RCollection selected, Query.CLASS<? extends Query> query) {
		Scheduler.restart(ApplicationServer.getDatabase());
	}
}
