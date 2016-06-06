package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.base.form.Desktop;
import org.zenframework.z8.server.base.job.scheduler.TaskLogs;
import org.zenframework.z8.server.base.table.system.view.UserEntriesView;
import org.zenframework.z8.server.db.generator.DBGenerateProcedure;
import org.zenframework.z8.server.ie.ExportMessages;
import org.zenframework.z8.server.ie.TransportRoutes;
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

    public final UserEntriesView.CLASS<UserEntriesView> userEntries = new UserEntriesView.CLASS<UserEntriesView>(this);
    public final TaskLogs.CLASS<TaskLogs> taskLogs = new TaskLogs.CLASS<TaskLogs>(this);
    public final Sequences.CLASS<Sequences> sequences = new Sequences.CLASS<Sequences>(this);
    public final DBGenerateProcedure.CLASS<DBGenerateProcedure> generator = new DBGenerateProcedure.CLASS<DBGenerateProcedure>(this);
    public final Properties.CLASS<Properties> properties = new Properties.CLASS<Properties>(this);
    public final ExportMessages.CLASS<ExportMessages> exportMessages = new ExportMessages.CLASS<ExportMessages>(this);
    public final TransportRoutes.CLASS<TransportRoutes> transportRoutes = new TransportRoutes.CLASS<TransportRoutes>(this);
    public final SystemFiles.CLASS<SystemFiles> files = new SystemFiles.CLASS<SystemFiles>(this);
    public final SystemDomains.CLASS<SystemDomains> addresses = new SystemDomains.CLASS<SystemDomains>(this);

    public SystemTools(IObject container) {
        super(container);
    }

    @Override
    public void constructor2() {
        super.constructor2();

        runnables.add(userEntries);
        runnables.add(taskLogs);
        runnables.add(sequences);
        runnables.add(generator);
        runnables.add(properties);
        runnables.add(exportMessages);
        runnables.add(transportRoutes);
        runnables.add(files);
        runnables.add(addresses);

        dataSets.add(userEntries);
        dataSets.add(taskLogs);
        dataSets.add(sequences);
        dataSets.add(properties);
        dataSets.add(exportMessages);
        dataSets.add(transportRoutes);
        dataSets.add(files);
        dataSets.add(addresses);
    }
}
