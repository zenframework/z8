package org.zenframework.z8.server.base.job.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.table.system.ScheduledJobs;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.MaintenanceJob;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.IDatabase;
import org.zenframework.z8.server.engine.Session;
import org.zenframework.z8.server.ie.ExchangeJob;
import org.zenframework.z8.server.ie.rmi.TransportJob;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.Request;
import org.zenframework.z8.server.types.guid;

public class Scheduler implements Runnable {
	private static Map<IDatabase, Scheduler> schedulers = new HashMap<IDatabase, Scheduler>();
	private static boolean destroying = false;
	private static Object mutex = new Object();

	private IDatabase database;
	private Thread thread = null;

	private boolean suspended = false;
	private boolean resetPending = true;
	
	private List<ScheduledJob> jobs = new ArrayList<ScheduledJob>();

	private List<ScheduledJob> systemJobs = new ArrayList<ScheduledJob>();
	private Collection<Thread> threads = new ArrayList<Thread>();

	static public Scheduler get(IDatabase database) {
		Scheduler scheduler = schedulers.get(database);

		if(scheduler == null) {
			scheduler = new Scheduler(database);
			schedulers.put(database, scheduler);
		}
		return scheduler;
	}

	static public void start(IDatabase database) {
		if(destroying || !ServerConfig.isSchedulerEnabled())
			return;

		synchronized(mutex) {
			Scheduler scheduler = get(database);
			scheduler.start();
		}
	}

	static public void resume(IDatabase database) {
		if(destroying || !ServerConfig.isSchedulerEnabled())
			return;

		synchronized(mutex) {
			Scheduler scheduler = get(database);
			scheduler.resume();
		}

		start(database);
	}

	static public void suspend(IDatabase database) {
		if(destroying || !ServerConfig.isSchedulerEnabled())
			return;

		synchronized(mutex) {
			Scheduler scheduler = get(database);
			scheduler.suspend();
		}

		stop(database);
	}

	static public void stop(IDatabase database) {
		if(destroying || !ServerConfig.isSchedulerEnabled())
			return;

		Scheduler scheduler = null;

		synchronized(mutex) {
			scheduler = get(database);
		}

		scheduler.stop();
	}

	static public synchronized void reset(IDatabase database) {
		if(destroying || !ServerConfig.isSchedulerEnabled())
			return;

		synchronized(mutex) {
			Scheduler scheduler = get(database);
			scheduler.reset();
		}
	}

	static public boolean register(IDatabase database, Thread thread) {
		if(destroying)
			return false;

		synchronized(mutex) {
			Scheduler scheduler = get(database);
			scheduler.register(thread);
		}

		return true;
	}

	static public boolean unregister(IDatabase database, Thread thread) {
		if(destroying)
			return false;

		synchronized(mutex) {
			Scheduler scheduler = get(database);
			scheduler.unregister(thread);
		}

		return true;
	}

	static public void destroy() {
		destroying = true;

		for(Scheduler scheduler : schedulers.values())
			scheduler.stop();

		schedulers.clear();
	}

	private Scheduler(IDatabase database) {
		this.database = database;
	}

	private void start() {
		if(suspended || thread != null || !database.isSystemInstalled() || !database.isLatestVersion())
			return;

		thread = new Thread(this, database.schema() + " scheduler");
		thread.start();
	}

	private void resume() {
		suspended = false;
	}

	private void suspend() {
		suspended = true;
	}

	private void reset() {
		resetPending = true;
	}

	private void stop() {
		if(thread != null) {
			thread.interrupt();
			thread = null;
		}
		this.stopJobs();
	}

	@Override
	public void run() {
		ApplicationServer.setRequest(new Request(new Session(database.schema())));

		while(thread != null) {
			initializeJobs();

			if(Thread.interrupted())
				break;

			startJobs();

			try {
				Thread.sleep(1 * 1000);
			} catch(InterruptedException e) {
				break;
			}
		}

		ApplicationServer.setRequest(null);
	}

	private void startJobs() {
		for(ScheduledJob job : jobs) {
			try {
				job.start();
			} catch(Throwable e) {
				Trace.logError(e);
			}
		}
	}

	private void stopJobs() {
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

		threads.clear();
	}

	private void initialzeSystemJobs() {
		if(systemJobs.isEmpty())
			return;

		if(ServerConfig.maintenanceJobEnabled())
			systemJobs.add(new ScheduledJob(MaintenanceJob.class.getCanonicalName(), ServerConfig.maintenanceJobCron(), database));
		if(ServerConfig.transportJobEnabled())
			systemJobs.add(new ScheduledJob(TransportJob.class.getCanonicalName(), ServerConfig.transportJobCron(),database));
		if(ServerConfig.exchangeJobEnabled())
			systemJobs.add(new ScheduledJob(ExchangeJob.class.getCanonicalName(), ServerConfig.exchangeJobCron(), database));
	}

	private void initializeJobs() {
		if(!resetPending)
			return;

		initialzeSystemJobs();

		ScheduledJobs scheduledJobs = new ScheduledJobs.CLASS<ScheduledJobs>(null).get();

		GuidField user = scheduledJobs.user.get();
		StringField cron = scheduledJobs.cron.get();
		DatetimeField lastStart = scheduledJobs.lastStart.get();
		DatetimeField nextStart = scheduledJobs.nextStart.get();
		BoolField active = scheduledJobs.active.get();
		BoolField logErrorsOnly = scheduledJobs.logErrorsOnly.get();
		StringField classId = scheduledJobs.jobs.get().classId.get();
		StringField name = scheduledJobs.jobs.get().name.get();

		Collection<Field> fields = Arrays.asList(user, cron, lastStart, nextStart, active, logErrorsOnly, classId, name);

		scheduledJobs.read(fields);

		List<ScheduledJob> result = new ArrayList<ScheduledJob>();

		while(scheduledJobs.next()) {
			guid id = scheduledJobs.recordId();
			ScheduledJob job = new ScheduledJob(id, database);

			int index = jobs.indexOf(job);
			if(index != -1)
				job = jobs.get(index);

			job.classId = classId.string().get();
			job.name = name.string().get();
			job.user = user.guid();
			job.lastStart = lastStart.date();
			job.nextStart = nextStart.date();
			job.cron = cron.string().get();
			job.active = active.bool().get();
			job.logErrorsOnly = logErrorsOnly.bool().get();

			result.add(job);
		}

		result.addAll(systemJobs);
		jobs = result;

		resetPending = false;

		ConnectionManager.release();
	}

	private void register(Thread thread) {
		threads.add(thread);
		thread.start();
	}

	private void unregister(Thread thread) {
		threads.remove(thread);
		thread.interrupt();
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
