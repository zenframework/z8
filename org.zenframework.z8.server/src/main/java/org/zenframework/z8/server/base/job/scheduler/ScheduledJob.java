package org.zenframework.z8.server.base.job.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.engine.Session;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.IMonitor;
import org.zenframework.z8.server.request.IRequest;
import org.zenframework.z8.server.request.IResponse;
import org.zenframework.z8.server.request.Request;
import org.zenframework.z8.server.request.RequestDispatcher;
import org.zenframework.z8.server.request.Response;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.User;

public class ScheduledJob implements Runnable {

	IRequest request = null;
	IResponse response = null;
	Task task = null;

	public ScheduledJob(Task task) {
		this.task = task;
	}

	@Override
	public void run() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(Json.requestId.get(), task.jobId);
		parameters.put(Json.scheduled.get(), "");

		List<FileInfo> files = new ArrayList<FileInfo>();

		try {
			task.start();

			IUser user = User.load(task.login);
			request = new Request(parameters, files, new Session("", user));
			response = new Response();

			new RequestDispatcher(request, response).run();
		} catch (Throwable e) {
			Trace.logError(e);
			IMonitor monitor = getMonitor();
			if (monitor != null)
				monitor.log(e);
		} finally {
			try {
				task.stop(getMonitor());
			} catch (Throwable e) {
				Trace.logError(e);
			}
		}
	}

	private IMonitor getMonitor() {
		return request != null ? request.getMonitor() : null;
	}
}
