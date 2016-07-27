package org.zenframework.z8.server.base.job.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.base.table.system.SchedulerJobs;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.ie.rmi.TransportJob;
import org.zenframework.z8.server.types.guid;

public class Scheduler implements Runnable {
	private static Scheduler scheduler = null;

	private Thread thread = null;
	private boolean resetPending = true;
	private List<SchedulerJob> jobs = new ArrayList<SchedulerJob>();
	
	static private List<SchedulerJob> systemJobs = new ArrayList<SchedulerJob>();
	static private Collection<Thread> threads = new ArrayList<Thread>();
	
	static {
		if(ServerConfig.transportJobEnabled())
			addSystemJob(TransportJob.class.getCanonicalName(), ServerConfig.transportJobRepeat());
	}

	static public void addSystemJob(String className, int repeat) {
		SchedulerJob job = new SchedulerJob(className, repeat);
		systemJobs.add(job);
	}

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

		SchedulerJobs jobsTable = new SchedulerJobs.CLASS<SchedulerJobs>(null).get();

		GuidField user = jobsTable.user.get();
		DatetimeField from = jobsTable.from.get();
		DatetimeField till = jobsTable.till.get();
		IntegerField repeat = jobsTable.repeat.get();
		DatetimeField lastStarted = jobsTable.lastStarted.get();
		BoolField active = jobsTable.active.get();
		StringField className = jobsTable.jobs.get().id.get();
		StringField name = jobsTable.jobs.get().name.get();

		Collection<Field> fields = Arrays.asList(user, from, till, repeat, lastStarted, active, className, name);

		jobsTable.read(fields);

		List<SchedulerJob> result = new ArrayList<SchedulerJob>();

		while(jobsTable.next()) {
			guid id = jobsTable.recordId();
			SchedulerJob job = new SchedulerJob(id);

			int index = jobs.indexOf(job);
			if(index != -1)
				job = jobs.get(index);

			job.className = className.string().get();
			job.name = name.string().get();
			job.user = user.guid();
			job.from = from.datetime();
			job.till = till.datetime();
			job.lastStarted = lastStarted.datetime();
			job.repeat = repeat.integer().getInt();
			job.active = active.bool().get();

			result.add(job);
		}

		result.addAll(systemJobs);
		jobs = result;

		resetPending = false;
	}

	private boolean hasRunningJobs() {
		for(SchedulerJob job : jobs) {
			if(job.isRunning)
				return true;
		}

		for(Thread thread : threads.toArray(new Thread[0])) {
			if(thread.isAlive())
				return true;
		}
		
		return false;
	}
}
