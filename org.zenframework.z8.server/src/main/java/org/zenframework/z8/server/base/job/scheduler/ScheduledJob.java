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
import org.zenframework.z8.server.utils.Cron;

public class ScheduledJob implements Runnable {
	public guid id;

	public String classId;
	public guid user;
	public String name;
	public String cron = "0 * * * *";
	public boolean active = true;

	public date lastStart = date.Min;
	public date nextStart = date.Min;

	public boolean isRunning = false;

	private int executionCount = 0;

	private Thread thread;

	public ScheduledJob(guid id) {
		this.id = id;
	}

	public ScheduledJob(String className, String cron) {
		this.classId = className;
		String[] names = className.split("\\.");
		this.name = names[names.length - 1];
		this.cron = cron;
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
		return !isRunning && active && !cron.isEmpty()
				&& Cron.nextDate(lastStart, cron).getTicks() < new date().getTicks();
	}

	private boolean beforeStart() {

		date lastStart = new date();
		date nextStart = Cron.nextDate(lastStart, cron);

		try {
			if(id != null) {
				ScheduledJobs tasks = new ScheduledJobs.CLASS<ScheduledJobs>(null).get();
				tasks.lastStart.get().set(lastStart);
				tasks.nextStart.get().set(nextStart);
				tasks.update(id);
			}

			this.lastStart = lastStart;
			this.nextStart = nextStart;

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
		logs.file.get().set(new string(writer.toString()));
		logs.start.get().set(lastStart);
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
