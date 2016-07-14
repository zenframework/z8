package org.zenframework.z8.server.base.job.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.table.system.Logs;
import org.zenframework.z8.server.base.table.system.SchedulerJobs;
import org.zenframework.z8.server.engine.Session;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.IRequest;
import org.zenframework.z8.server.request.IResponse;
import org.zenframework.z8.server.request.Request;
import org.zenframework.z8.server.request.RequestDispatcher;
import org.zenframework.z8.server.request.Response;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.User;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class SchedulerJob implements Runnable {
	public guid id;

	public String className;
	public String login;
	public String name;
	public String settings;
	public datetime from = new datetime();
	public datetime till = new datetime().addDay(30);
	public int repeat = 1;
	public boolean active = true;

	public datetime lastStarted = new datetime();

	public boolean isRunning = false;

	private int executionCount = 0;

	private Thread thread;
	
	public SchedulerJob(guid id) {
		this.id = id;
	}

	public SchedulerJob(String className, int repeat) {
		this.className = className;
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
		if(object instanceof SchedulerJob) {
			SchedulerJob task = (SchedulerJob)object;
			return id.equals(task.id);
		}
		return false;
	}

	public boolean readyToStart() {
		long now = new datetime().getTicks();

		return active && !isRunning && from.getTicks() < now && 
				(till.equals(datetime.MIN) || now < till.getTicks()) && 
				(lastStarted.equals(datetime.MIN) || lastStarted.addSecond(repeat).getTicks() < now);
	}

	private boolean beforeStart() {

		datetime lastStarted = new datetime();

		try {
			if(id != null) {
				SchedulerJobs tasks = new SchedulerJobs.CLASS<SchedulerJobs>(null).get();
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

	private void afterFinish(file log) {
		if(id == null || log == null)
			return;

		JsonArray writer = new JsonArray();
		log.name = new string("error.log");
		writer.put(log);

		Logs logs = Logs.newInstance();
		logs.job.get().set(id);
		logs.files.get().set(new string(writer.toString()));
		logs.started.get().set(lastStarted);
		logs.finished.get().set(new datetime());
		logs.create();
	}
	
	@Override
	public void run() {
		try {
			isRunning = true;

			if(!beforeStart())
				return;

			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put(Json.requestId.get(), className);
			parameters.put(Json.scheduled.get(), "true");
			parameters.put(Json.settings.get(), settings);
	
			List<file> files = new ArrayList<file>();
	
			IUser user = login != null ? User.load(login) : User.system();
			IRequest request = new Request(parameters, files, new Session("", user));
			IResponse response = new Response();
	
			new RequestDispatcher(request, response).run();
	
			afterFinish(request.getMonitor().getLog());
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
