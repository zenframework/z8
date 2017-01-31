package org.zenframework.z8.server.request;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import org.zenframework.z8.server.base.Procedure;
import org.zenframework.z8.server.base.job.Job;
import org.zenframework.z8.server.base.job.JobMonitor;
import org.zenframework.z8.server.base.model.actions.RequestAction;
import org.zenframework.z8.server.base.model.actions.ActionFactory;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.view.Dashboard;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.utils.ErrorUtils;

public class RequestDispatcher implements Runnable {

	private IRequest request;
	private IResponse response;

	public RequestDispatcher(IRequest request, IResponse response) {
		this.request = request;
		this.response = response;
	}

	@Override
	public void run() {
		try {
			ApplicationServer.setRequest(request);
			dispatch();
		} catch(Throwable exception) {
			Trace.logError(request.toString(), exception);

			JsonWriter writer = new JsonWriter();
			writer.startResponse(request.id(), false);
			writer.startArray(Json.data);
			writer.finishArray();

			IMonitor monitor = request.getMonitor();
			monitor.error(ErrorUtils.getMessage(exception));
			monitor.log(exception);

			try {
				monitor.writeResponse(writer);
			} catch(Throwable e) {
				Trace.logError(e);
			}

			writer.finishResponse();

			response.setContent(writer.toString());
		} finally {
			ConnectionManager.get().release();
			ApplicationServer.setRequest(null);
		}
	}

	private void dispatch() throws Throwable {
		String requestId = request.id();
		String jobId = request.getParameter(Json.job);

		if(jobId != null) {
			JobMonitor monitor = Job.getMonitor(jobId);

			if(monitor == null) {
				monitor = new JobMonitor(null, jobId);
				monitor.setTotal(100);
				monitor.setWorked(100);
			}

			IRequest request = ApplicationServer.getRequest();
			request.setTarget(monitor);
			request.setMonitor(monitor);
			monitor.processRequest(response);
		} else {
			long t = System.currentTimeMillis();

			processRequest(request, response, requestId);

			if(!Json.login.equals(requestId) && !Json.settings.equals(requestId))
				Trace.logEvent(request.toString() + "\n\t " + (System.currentTimeMillis() - t) + "ms; " + getMemoryUsage());
		}
	}

	public static String getMemoryUsage() {
		MemoryMXBean memoryMxBean = ManagementFactory.getMemoryMXBean();
		MemoryUsage heap = memoryMxBean.getHeapMemoryUsage();

		return (heap.getUsed() >> 20) + " of " + (heap.getCommitted() >> 20) + "M";
	}

	private void processRequest(IRequest request, IResponse response, String requestId) throws Throwable {
		if(Json.login.equals(requestId)) {
			Dashboard dashboard = new Dashboard();
			dashboard.processRequest(response);
		} else if(Json.settings.equals(requestId)) {
			IUser user = ApplicationServer.getUser();
			user.setSettings(request.getParameter(Json.data));

			user.save();

			JsonWriter writer = new JsonWriter();
			writer.startResponse(requestId, true);
			writer.writeInfo();
			writer.finishResponse();

			response.setContent(writer.toString());
		} else {
			OBJECT object = requestId != null ? Loader.getInstance(requestId) : null;

			request.setTarget(object);

			if(object != null && object.response() != null) {
				object.processRequest(response);
			} else if(object instanceof Query) {
				Query query = (Query)object;
				RequestAction action = ActionFactory.create(query);
				action.processRequest(response);
			} else if(object instanceof Procedure) {
				Procedure procedure = (Procedure)object;
				Job job = new Job(procedure);
				job.processRequest(response);
			}
		}
	}
}
