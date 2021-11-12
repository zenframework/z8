package org.zenframework.z8.server.base.job;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.Executable;
import org.zenframework.z8.server.base.form.action.IParameter;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.request.RequestTarget;
import org.zenframework.z8.server.types.guid;

public class Job extends RequestTarget {

	private static Map<String, JobMonitor> monitors = Collections.synchronizedMap(new HashMap<String, JobMonitor>());

	private final Executable executable;

	private Thread thread;
	private JobMonitor monitor;
	private boolean isDone = false;

	public Job(Executable executable) {
		super(executable.classId());
		this.executable = executable;
		monitor = createMonitor(this);
	}

	@Override
	public String displayName() {
		return executable.displayName();
	}

	public Thread getThread() {
		return thread;
	}

	public boolean scheduled() {
		return getParameter(Json.scheduled) != null;
	}

	@Override
	public void writeResponse(JsonWriter writer) {
		setParameters();

		if(!scheduled()) {
			thread = new Thread(executable, executable.displayName());
			thread.start();

			writer.writeProperty(Json.id, monitor.id());
			writer.writeProperty(Json.server, ApplicationServer.id);
			writer.writeProperty(Json.text, executable.displayName());
		} else {
			executable.run();
			isDone = true;
			removeMonitor(monitor);
		}
	}

	public boolean isDone() {
		return scheduled() ? isDone : !thread.isAlive();
	}

	private void setParameters() {
		String data = getParameter(Json.parameters);

		if(data == null)
			return;

		JsonObject object = new JsonObject(data);

		for(String parameterId : object.getNames()) {
			IParameter parameter = executable.getParameter(parameterId);
			parameter.parse(object.getString(parameterId));
		}
	}

	static public JobMonitor getMonitor(String id) {
		return monitors.get(id);
	}

	static public void removeMonitor(JobMonitor monitor) {
		monitors.remove(monitor.id());
	}

	static private JobMonitor createMonitor(Job job) {
		JobMonitor monitor = new JobMonitor(job, guid.create().toString());
		ApplicationServer.getRequest().setMonitor(monitor);
		monitors.put(monitor.id(), monitor);
		return monitor;
	}
}
