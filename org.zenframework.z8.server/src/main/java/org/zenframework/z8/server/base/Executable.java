package org.zenframework.z8.server.base;

import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.form.action.Parameter;
import org.zenframework.z8.server.base.job.JobMonitor;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.engine.Session;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.request.IRequest;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.security.User;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class Executable extends Action {
	public static class CLASS<T extends Executable> extends Action.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Executable.class);
			setExecutable(true);
		}

		@Override
		public Object newObject(IObject container) {
			return new Executable(container);
		}
	}

	private IRequest request;
	private ISession defaultSession;

	public Executable(IObject container) {
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

	protected void execute() {
		z8_execute();
	}

	protected void z8_execute() {
		z8_execute(parameters);
	}

	protected void z8_execute(RCollection<Parameter.CLASS<? extends Parameter>> parameters) {}

	protected void switchUser(guid user) {
		if (!defaultSession.user().isBuiltinAdministrator())
			throw new AccessDeniedException();
		if (!request.getSession().user().id().equals(user))
			request.setSession(new Session("", User.read(user)));
	}

	protected void z8_switchUser(guid user) {
		switchUser(user);
	}

	protected void resetUser() {
		if (request.getSession() != defaultSession)
			request.setSession(defaultSession);
	}

	protected void z8_resetUser() {
		resetUser();
	}

	@Override
	public void run() {
		ApplicationServer.setRequest(request);

		defaultSession = request.getSession();
		Connection connection = null;

		try {
			connection = useTransaction.get() ? ConnectionManager.get() : null;

			if(connection != null)
				connection.beginTransaction();

			execute();

			if(connection != null)
				connection.commit();
		} catch(Throwable e) {
			if(connection != null)
				connection.rollback();
			getMonitor().error(e);
		} finally {
			resetUser();
		}
	}

	public JobMonitor getMonitor() {
		return (JobMonitor)ApplicationServer.getMonitor();
	}

	@Override
	public void write(JsonWriter writer) {
		super.write(writer);
		writer.writeProperty(Json.isJob, true);
	}
}
