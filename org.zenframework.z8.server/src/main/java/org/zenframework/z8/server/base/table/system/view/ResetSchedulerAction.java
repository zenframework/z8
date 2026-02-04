package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.job.scheduler.Scheduler;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.system.ScheduledJobs;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.guid;

public class ResetSchedulerAction extends Action {
	static public class CLASS<T extends ResetSchedulerAction> extends Action.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(ResetSchedulerAction.class);
			setDisplayName(ScheduledJobs.displayNames.ResetScheduler);
		}

		@Override
		public Object newObject(IObject container) {
			return new ResetSchedulerAction(container);
		}
	}

	public ResetSchedulerAction(IObject container) {
		super(container);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void z8_execute(guid recordId, Query.CLASS<? extends Query> context, RCollection selected, Query.CLASS<? extends Query> query) {
		Scheduler.reset(ApplicationServer.getDatabase());
	}
}
