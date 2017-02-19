package org.zenframework.z8.server.base.job.scheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.table.system.ScheduledJobLogs;
import org.zenframework.z8.server.base.table.system.ScheduledJobs;
import org.zenframework.z8.server.engine.Session;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.IRequest;
import org.zenframework.z8.server.request.IResponse;
import org.zenframework.z8.server.request.Request;
import org.zenframework.z8.server.request.RequestDispatcher;
import org.zenframework.z8.server.request.Response;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.User;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class ScheduledJob implements Runnable {
	public guid id;

	public String classId;
	public guid user;
	public String name;
	public date from = new date();
	public date till = new date().addDay(30);
	public int repeat = 1;
	public boolean active = true;

	public date lastStarted = new date();

	public boolean isRunning = false;

	private int executionCount = 0;

	private Thread thread;

	public ScheduledJob(guid id) {
		this.id = id;
	}

	public ScheduledJob(String className, int repeat) {
		this.classId = className;
		String[] names = className.split("\\.");
		this.name = names[names.length - 1];
		this.repeat = repeat;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return name + "-" + (executionCount + 1);
	}

	@Override
	public boolean equals(Object object) {
		if(object instanceof ScheduledJob) {
			ScheduledJob task = (ScheduledJob)object;
			return id.equals(task.id);
		}
		return false;
	}

	public boolean readyToStart() {
		long now = new date().getTicks();

		return active && !isRunning && from.getTicks() < now && (till.equals(date.Min) || now < till.getTicks()) && (lastStarted.equals(date.Min) || lastStarted.addSecond(repeat).getTicks() < now);
	}

	private boolean beforeStart() {

		date lastStarted = new date();

		try {
			if(id != null) {
				ScheduledJobs tasks = new ScheduledJobs.CLASS<ScheduledJobs>(null).get();
				tasks.lastStarted.get().set(lastStarted);
				tasks.update(id);
			}

			this.lastStarted = lastStarted;

			executionCount++;

			return true;
		} catch(Throwable e) {
			Trace.logError(e);
			return false;
		}
	}

	private void afterFinish(Collection<file> files) {
		if(id == null || files.isEmpty())
			return;

		JsonWriter writer = new JsonWriter();
		writer.startArray();
		for(file file : files) {
			writer.startObject();
			file.write(writer);
			writer.finishObject();
		}
		writer.finishArray();

		ScheduledJobLogs logs = ScheduledJobLogs.newInstance();
		logs.scheduledJob.get().set(id);
		logs.files.get().set(new string(writer.toString()));
		logs.start.get().set(lastStarted);
		logs.finish.get().set(new date());
		logs.create();
	}

	@Override
	public void run() {
		try {
			isRunning = true;

			if(!beforeStart())
				return;

			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put(Json.request.get(), classId);
			parameters.put(Json.scheduled.get(), "true");

			List<file> files = new ArrayList<file>();

			IUser user = this.user != null ? User.read(this.user) : User.system();
			IRequest request = new Request(parameters, files, new Session("", user));
			IResponse response = new Response();

			new RequestDispatcher(request, response).run();

			afterFinish(request.getMonitor().getFiles());
		} finally {
			isRunning = false;
			thread = null;
		}
	}

	public void start() {
		if(readyToStart()) {
			thread = new Thread(this, toString());
			thread.start();
		}
	}

	public void stop() {
		if(thread != null)
			thread.interrupt();
	}
}
