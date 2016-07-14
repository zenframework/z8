package org.zenframework.z8.server.base.job.scheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.base.table.system.SchedulerJobs;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.base.table.value.TextField;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.types.guid;

public class Scheduler implements Runnable {

	private static Scheduler scheduler = null;

	private Thread thread = null;
	private boolean resetPending = true;
	private List<SchedulerJob> jobs = new ArrayList<SchedulerJob>();

	static private Collection<Thread> threads = new ArrayList<Thread>();
	
	static public synchronized void register(Thread thread) {
		threads.add(thread);
	}
	
	static public synchronized void unregister(Thread thread) {
		threads.remove(thread);
	}

	static public void start() {
		if(scheduler == null && ServerConfig.isSchedulerEnabled() && ServerConfig.database().isSystemInstalled())
			scheduler = new Scheduler();
	}

	static public void stop() {
		if(scheduler != null) {
			scheduler.stopJobs();
			scheduler = null;
		}
	}

	static public void reset() {
		if(scheduler != null)
			scheduler.resetPending = true;
	}

	private Scheduler() {
		thread = new Thread(this, "Z8 scheduler");
		thread.start();
	}

	@Override
	public void run() {
		while(scheduler != null) {
			initializeJobs();

			if(Thread.interrupted())
				return;

			for(SchedulerJob job : jobs)
				job.start();

			try {
				Thread.sleep(1 * 1000);
			} catch(InterruptedException e) {
				return;
			}
		}
	}

	private void stopJobs() {
		thread.interrupt();

		for(SchedulerJob job : jobs.toArray(new SchedulerJob[0]))
			job.stop();

		for(Thread thread : threads.toArray(new Thread[0]))
			thread.interrupt();

		while(hasRunningJobs()) {
			try {
				Thread.sleep(100);
			} catch(InterruptedException e) {
			}
		}
	}

	private synchronized void initializeJobs() {
		if(!resetPending || !ServerConfig.isSystemInstalled())
			return;

		SchedulerJobs tasksTable = new SchedulerJobs.CLASS<SchedulerJobs>(null).get();

		Collection<Field> fields = new ArrayList<Field>();

		TextField settings = tasksTable.description.get();
		DatetimeField from = tasksTable.from.get();
		DatetimeField till = tasksTable.till.get();
		IntegerField repeat = tasksTable.repeat.get();
		DatetimeField lastStarted = tasksTable.lastStarted.get();
		BoolField active = tasksTable.active.get();
		StringField jobId = tasksTable.jobs.get().id.get();
		StringField jobName = tasksTable.jobs.get().name.get();
		StringField login = tasksTable.users.get().name.get();

		fields.add(settings);
		fields.add(from);
		fields.add(till);
		fields.add(repeat);
		fields.add(lastStarted);
		fields.add(active);
		fields.add(jobId);
		fields.add(jobName);
		fields.add(login);

		tasksTable.read(fields);

		List<SchedulerJob> result = new ArrayList<SchedulerJob>();

		while(tasksTable.next()) {
			guid taskId = tasksTable.recordId();
			SchedulerJob task = new SchedulerJob(taskId);

			int index = jobs.indexOf(task);
			if(index != -1)
				task = jobs.get(index);

			task.jobId = jobId.string().get();
			task.name = jobName.string().get();
			task.login = login.string().get();
			task.from = from.datetime();
			task.settings = settings.string().get();
			task.till = till.datetime();
			task.lastStarted = lastStarted.datetime();
			task.repeat = repeat.integer().getInt();
			task.active = active.bool().get();

			result.add(task);
		}

		jobs = result;

		resetPending = false;
	}

	private boolean hasRunningJobs() {
		for(SchedulerJob task : jobs) {
			if(task.isRunning)
				return true;
		}

		for(Thread thread : threads.toArray(new Thread[0])) {
			if(thread.isAlive())
				return true;
		}
		
		return false;
	}
}
