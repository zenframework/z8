package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.job.scheduler.ScheduledJob;
import org.zenframework.z8.server.base.job.scheduler.Scheduler;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.system.TransportQueue;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.ie.rmi.TransportJob;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.guid;

public class RestartTransportAction extends Action {
	static public class CLASS<T extends RestartTransportAction> extends Action.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(RestartTransportAction.class);
			setDisplayName(TransportQueue.displayNames.RestartTransport);
		}

		@Override
		public Object newObject(IObject container) {
			return new RestartTransportAction(container);
		}
	}

	public RestartTransportAction(IObject container) {
		super(container);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void z8_execute(guid recordId, Query.CLASS<? extends Query> context, RCollection selected, Query.CLASS<? extends Query> query) {
		ScheduledJob transportJob = Scheduler.get(ApplicationServer.getDatabase()).findSystemJob(TransportJob.class.getCanonicalName());
		if(transportJob != null)
			transportJob.restart();
	}
}
