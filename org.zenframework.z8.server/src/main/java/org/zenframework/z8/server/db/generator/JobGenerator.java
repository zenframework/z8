package org.zenframework.z8.server.db.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.table.system.Jobs;
import org.zenframework.z8.server.base.table.system.Logs;
import org.zenframework.z8.server.base.table.system.SchedulerJobs;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class JobGenerator {

	public void run(ILogger logger) {
		createJobs(logger);
		deleteOldJobs(logger);
	}

	private void createJobs(ILogger logger) {
		Collection<Procedure.CLASS<? extends Procedure>> jobs = Runtime.instance().jobs();

		Jobs jobsTable = new Jobs.CLASS<Jobs>().get();
		SchedulerJobs schedulerJobs = new SchedulerJobs.CLASS<SchedulerJobs>().get();

		for(Procedure.CLASS<? extends Procedure> jobClass : jobs) {
			String classId = jobClass.classId();
			guid jobRecordId;

			jobsTable.read(new Equ(jobsTable.id.get(), classId));

			jobsTable.id.get().set(new string(classId));
			jobsTable.name.get().set(new string(jobClass.displayName()));
			if(jobClass.hasAttribute(IObject.Settings))
				jobsTable.description.get().set(jobClass.getAttribute(IObject.Settings));

			if(jobsTable.next()) {
				jobRecordId = jobsTable.recordId();
				jobsTable.update(jobRecordId);
			} else
				jobRecordId = jobsTable.create();

			String jobValue = jobClass.getAttribute(IObject.Job);
			
			if(jobValue != null && !jobValue.isEmpty()) {
				integer repeat = new integer(jobValue);
				if(!schedulerJobs.readFirst(new Equ(schedulerJobs.job.get(), jobRecordId))) {
					schedulerJobs.active.get().set(new bool(repeat.get() >= 0));
					schedulerJobs.job.get().set(jobRecordId);
					if(repeat.get() >= 0)
						schedulerJobs.repeat.get().set(repeat);
					schedulerJobs.create();
				}
			}
		}
	}

	private Collection<String> jobs() {
		Collection<String> result = new ArrayList<String>();
		
		for(Procedure.CLASS<? extends Procedure> cls : Runtime.instance().jobs())
			result.add(cls.classId());
		
		return result;
	}
	
	private void deleteOldJobs(ILogger logger) {
		Collection<String> jobs = jobs();
		
		SchedulerJobs schedulerJobs = new SchedulerJobs.CLASS<SchedulerJobs>().get();
		Field jobId = schedulerJobs.jobs.get().id.get();
		
		schedulerJobs.read(Arrays.asList(jobId));
		
		Logs logs = new Logs.CLASS<Logs>().get();

		while(schedulerJobs.next()) {
			if(!jobs.contains(jobId.string().get())) {
				guid recordId = schedulerJobs.recordId();
				logs.destroy(new Equ(logs.job.get(), recordId));
				schedulerJobs.destroy(recordId);
			}				
		}
	}
}
