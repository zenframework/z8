package org.zenframework.z8.server.base;

import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.form.action.Parameter;
import org.zenframework.z8.server.base.job.JobMonitor;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.IRequest;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.ErrorUtils;

public class Procedure extends Action {
	public static class CLASS<T extends Procedure> extends Action.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Procedure.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Procedure(container);
		}
	}

	private IRequest request;

	public Procedure(IObject container) {
		super(container);
		request = ApplicationServer.getRequest();
	}

	public void reportProgress(int percentDone) {
		JobMonitor monitor = getMonitor();
		monitor.setWorked(percentDone);
	}

	public void info(String message) {
		JobMonitor monitor = getMonitor();
		monitor.info(message);
	}

	public void warning(String message) {
		JobMonitor monitor = getMonitor();
		monitor.warning(message);
	}

	public void error(String message) {
		JobMonitor monitor = getMonitor();
		monitor.error(message);
	}

	public void z8_progress(integer percentDone) {
		reportProgress(percentDone.getInt());
	}

	public void z8_info(string message) {
		info(message.get());
	}

	public void z8_warning(string message) {
		warning(message.get());
	}

	public void z8_error(string message) {
		error(message.get());
	}

	protected void z8_execute() {
		z8_execute(parameters);
	}

	protected void z8_execute(RCollection<Parameter.CLASS<? extends Parameter>> parameters) {
	}

	@Override
	public void run() {
		ApplicationServer.setRequest(request);

		Connection connection = useTransaction.get() ? ConnectionManager.get() : null;

		try {
			if(connection != null)
				connection.beginTransaction();

			z8_execute();

			if(connection != null)
				connection.commit();
		} catch(Throwable e) {
			if(connection != null)
				connection.rollback();
		} finally {
			getMonitor().logMessages();
		}
	}

	protected void log(String message, Throwable e) {
		log(new exception(message, e));
	}

	protected void log(Throwable e) {
		Trace.logError(e);

		JobMonitor monitor = getMonitor();
		monitor.setWorked(monitor.getTotal());
		monitor.log(e);
		monitor.error(Resources.format("Procedure.jobError", ErrorUtils.getMessage(e)));

		if(useTransaction.get())
			monitor.info(Resources.get("Procedure.rollback"));
	}

	public JobMonitor getMonitor() {
		return (JobMonitor) ApplicationServer.getMonitor();
	}

	@Override
	public String displayName() {
		return getCLASS().displayName();
	}

	@Override
	public void write(JsonWriter writer) {
		super.write(writer);

		writer.writeProperty(Json.isJob, true);
		writer.writeProperty(Json.id, classId());
	}
}
