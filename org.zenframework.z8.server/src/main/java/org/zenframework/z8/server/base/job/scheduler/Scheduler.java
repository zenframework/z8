package org.zenframework.z8.server.base.job.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.base.table.system.ScheduledJobs;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.MaintenanceJob;
import org.zenframework.z8.server.ie.rmi.TransportJob;
import org.zenframework.z8.server.types.guid;

public class Scheduler implements Runnable {
	private static Scheduler scheduler = null;

	private Thread thread = null;
	private boolean resetPending = true;
	private List<ScheduledJob> jobs = new ArrayList<ScheduledJob>();

	static private List<ScheduledJob> systemJobs = new ArrayList<ScheduledJob>();
	static private Collection<Thread> threads = new ArrayList<Thread>();

	static {
		if(ServerConfig.maintenanceJobEnabled())
			addSystemJob(MaintenanceJob.class.getCanonicalName(), ServerConfig.maintenanceJobRepeat());
		if(ServerConfig.transportJobEnabled())
			addSystemJob(TransportJob.class.getCanonicalName(), ServerConfig.transportJobRepeat());
	}

	static public void addSystemJob(String className, int repeat) {
		ScheduledJob job = new ScheduledJob(className, repeat);
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

			for(ScheduledJob job : jobs)
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

		for(ScheduledJob job : jobs.toArray(new ScheduledJob[0]))
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
		if(!resetPending || !ServerConfig.isLatestVersion())
			return;

		ScheduledJobs scheduledJobs = new ScheduledJobs.CLASS<ScheduledJobs>(null).get();

		GuidField user = scheduledJobs.user.get();
		DatetimeField from = scheduledJobs.from.get();
		DatetimeField till = scheduledJobs.till.get();
		IntegerField repeat = scheduledJobs.repeat.get();
		DatetimeField lastStarted = scheduledJobs.lastStarted.get();
		BoolField active = scheduledJobs.active.get();
		StringField classId = scheduledJobs.jobs.get().classId.get();
		StringField name = scheduledJobs.jobs.get().name.get();

		Collection<Field> fields = Arrays.asList(user, from, till, repeat, lastStarted, active, classId, name);

		scheduledJobs.read(fields);

		List<ScheduledJob> result = new ArrayList<ScheduledJob>();

		while(scheduledJobs.next()) {
			guid id = scheduledJobs.recordId();
			ScheduledJob job = new ScheduledJob(id);

			int index = jobs.indexOf(job);
			if(index != -1)
				job = jobs.get(index);

			job.classId = classId.string().get();
			job.name = name.string().get();
			job.user = user.guid();
			job.from = from.date();
			job.till = till.date();
			job.lastStarted = lastStarted.date();
			job.repeat = repeat.integer().getInt();
			job.active = active.bool().get();

			result.add(job);
		}

		result.addAll(systemJobs);
		jobs = result;

		resetPending = false;

		ConnectionManager.release();
	}

	private boolean hasRunningJobs() {
		for(ScheduledJob job : jobs) {
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
