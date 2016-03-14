package org.zenframework.z8.server.base.simple;

import org.zenframework.z8.server.base.job.JobMonitor;
import org.zenframework.z8.server.base.view.command.Command;
import org.zenframework.z8.server.base.view.command.Parameter;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.IRequest;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.ErrorUtils;

public class Procedure extends Command implements java.lang.Runnable {
	public static class CLASS<T extends Procedure> extends Command.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Procedure.class);
			setAttribute(Native, Procedure.class.getCanonicalName());
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

	public void reportProgress(String message, int percentDone) {
		JobMonitor monitor = getMonitor();
		monitor.setWorked(percentDone);
		monitor.print(message);
	}

	public void z8_setProfilerStatus(string status, integer percentDone) {
		if(!status.get().isEmpty()) {
			reportProgress(status.get(), percentDone.getInt());
		} else {
			reportProgress(percentDone.getInt());
		}
	}

	public void z8_setProfilerText(string text) {
		print(text.get());
	}

	@Deprecated
	public void z8_setProfilerText(string text, integer percent) {
		z8_setProfilerStatus(text, percent);
	}

	protected void z8_exec() {
		z8_exec(parameters);
	}

	protected void z8_exec(RCollection<Parameter.CLASS<? extends Parameter>> parameters) {
	}

	public void z8_onError(exception e) {
		log(e);
	}

	// ///////////////////////////////////////////////////////////////

	@Override
	public void run() {
		ApplicationServer.setRequest(request);

		Connection connection = useTransaction.get() ? ConnectionManager.get() : null;

		try {
			if(connection != null)
				connection.beginTransaction();

			z8_exec();

			if(connection != null)
				connection.commit();
		} catch(Throwable e) {
			if(connection != null)
				connection.rollback();

			z8_onError(new exception(e));
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
		monitor.setWorked(monitor.getTotalWork());
		monitor.log(e);
		monitor.print(Resources.format("Procedure.jobError", ErrorUtils.getMessage(e)));
		
		if(useTransaction.get())
			monitor.print(Resources.get("Procedure.rollback"));
	}

	public void print(String text) {
		JobMonitor monitor = getMonitor();

		if(monitor != null)
			monitor.print(text);
	}

	public JobMonitor getMonitor() {
		return (JobMonitor) ApplicationServer.getMonitor();
	}

	@Override
	public String displayName() {
		return getCLASS().displayName();
	}

	@Override
	public void write(JsonObject writer) {
		id.set(classId());
		text.set(displayName());
		description.set(description());

		writer.put(Json.isJob, true);

		super.write(writer);
	}
}
