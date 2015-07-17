package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.form.Desktop;
import org.zenframework.z8.server.base.job.scheduler.TaskLogs;
import org.zenframework.z8.server.base.table.system.view.UserEntriesView;
import org.zenframework.z8.server.db.generator.DBGenerateProcedure;
import org.zenframework.z8.server.ie.ExportMessages;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.guid;

public class SystemTools extends Desktop {
    final static public guid Id = new guid("00000000-0000-0000-0000-000000000001");

    static public class strings {
        public final static String Title = "SystemTools.title";
    }

    public static class CLASS<T extends SystemTools> extends Desktop.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(SystemTools.class);
            setDisplayName(Resources.get(strings.Title));
        }

        @Override
        public Object newObject(IObject container) {
            return new SystemTools(container);
        }
    }

    public SystemTools(IObject container) {
        super(container);
    }

    @Override
    public void constructor2() {
        super.constructor2();

        runnables.add(new UserEntriesView.CLASS<UserEntriesView>(this));
        runnables.add(new TaskLogs.CLASS<TaskLogs>(this));
        runnables.add(new Sequences.CLASS<Sequences>(this));
        runnables.add(new DBGenerateProcedure.CLASS<DBGenerateProcedure>(this));
        runnables.add(new Properties.CLASS<Properties>(this));
        runnables.add(new ExportMessages.CLASS<ExportMessages>(this));
    }
}
