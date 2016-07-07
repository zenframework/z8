package org.zenframework.z8.server.db.generator;

import java.util.Collection;

import org.zenframework.z8.server.base.job.scheduler.Jobs;
import org.zenframework.z8.server.base.job.scheduler.Tasks;
import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class JobGenerator {

	public void run(ILogger logger) {
        Collection<Procedure.CLASS<? extends Procedure>> jobs = Runtime.instance().jobs();

        Jobs jobsTable = new Jobs.CLASS<Jobs>().get();
        Tasks tasksTable = new Tasks.CLASS<Tasks>().get();

        for (Procedure.CLASS<? extends Procedure> jobClass : jobs) {
            String classId = jobClass.classId();
            guid jobRecordId;

            jobsTable.read(new Equ(jobsTable.id.get(), classId));

            jobsTable.id.get().set(new string(classId));
            jobsTable.name.get().set(new string(jobClass.displayName()));
            if (jobClass.hasAttribute(IObject.Settings))
                jobsTable.description.get().set(jobClass.getAttribute(IObject.Settings));

            if(jobsTable.next()) {
                jobRecordId = jobsTable.recordId();
                jobsTable.update(jobRecordId);
            } else {
                jobRecordId = jobsTable.create();
            }
            
            String jobValue = jobClass.getAttribute(IObject.Job);
            if (jobValue != null && !jobValue.isEmpty()) {
                integer repeat = new integer(jobValue);
                if (!tasksTable.readFirst(new Equ(tasksTable.job.get(), jobRecordId))) {
                    tasksTable.active.get().set(new bool(repeat.get() >= 0));
                    tasksTable.job.get().set(jobRecordId);
                    if (repeat.get() >= 0)
                        tasksTable.repeat.get().set(repeat);
                    tasksTable.create();
                }
            }
        }
    }

}
