package org.zenframework.z8.server.db.generator;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.zenframework.z8.server.base.query.RecordLock;
import org.zenframework.z8.server.base.table.system.Jobs;
import org.zenframework.z8.server.base.table.system.ScheduledJobLogs;
import org.zenframework.z8.server.base.table.system.ScheduledJobs;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.ErrorUtils;

public class JobGenerator {
	@SuppressWarnings("unused")
	private ILogger logger;

	private Jobs jobs = new Jobs.CLASS<Jobs>().get();
	private ScheduledJobs scheduledJobs = new ScheduledJobs.CLASS<ScheduledJobs>().get();
	private ScheduledJobLogs scheduledJobLogs = new ScheduledJobLogs.CLASS<ScheduledJobLogs>().get();

	private Collection<guid> jobKeys = new HashSet<guid>();

	public JobGenerator(ILogger logger) {
		this.logger = logger;
		jobKeys.addAll(Runtime.instance().jobKeys());
	}

	public void run() {
		Connection connection = null;

		try {
			connection = ConnectionManager.get();
			connection.beginTransaction();
			dropJobs();
			createJobs();
			connection.commit();
		} catch(Throwable e) {
			if (connection != null)
				connection.rollback();
			logger.error(e, ErrorUtils.getMessage(e));
		} finally {
			if (connection != null)
				connection.release();
		}
	}

	private void dropJobs() {
		jobs.read(Arrays.asList(jobs.primaryKey()), jobs.primaryKey().notInVector(jobKeys));

		while(jobs.next()) {
			guid job = jobs.recordId();
			scheduledJobLogs.destroy(new Equ(scheduledJobLogs.scheduledJobs.get().job.get(), job));
			scheduledJobs.destroy(new Equ(scheduledJobs.job.get(), job));
			jobs.destroy(job);
		}
	}

	private void createJobs() {
		jobs.read(Arrays.asList(jobs.primaryKey()), jobs.primaryKey().inVector(jobKeys));

		while (jobs.next()) {
			guid job = jobs.recordId();
			setJobProperties(Runtime.instance().getJobByKey(job).newInstance());
			jobs.update(job);
			jobKeys.remove(job);
		}

		for (guid key : jobKeys) {
			setJobProperties(Runtime.instance().getJobByKey(key).newInstance());
			jobs.create(key);
		}
	}

	private void setJobProperties(OBJECT job) {
		jobs.classId.get().set(job.classId());
		jobs.name.get().set(new string(job.displayName()));
		jobs.lock.get().set(RecordLock.Destroy);

		String jobCron = job.getAttribute(IObject.Job);

		if (jobCron == null)
			return;

		if (!scheduledJobs.hasRecord(new Equ(scheduledJobs.job.get(), job.key()))) {
			scheduledJobs.active.get().set(new bool(!jobCron.isEmpty()));
			scheduledJobs.job.get().set(job.key());
			scheduledJobs.cron.get().set(jobCron);
			scheduledJobs.create();
		}
	}
}
