package org.zenframework.z8.server.db.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.form.Desktop;
import org.zenframework.z8.server.base.job.scheduler.Scheduler;
import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.security.SecurityGroup;
import org.zenframework.z8.server.utils.ErrorUtils;

public class DBGenerateProcedure extends Procedure {
    public static class CLASS<T extends DBGenerateProcedure> extends Procedure.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(DBGenerateProcedure.class);
            setDisplayName(Resources.get("Generator.displayName"));
        }

        @Override
        public Object newObject(IObject container) {
            return new DBGenerateProcedure(container);
        }
    }

    public DBGenerateProcedure(IObject container) {
        super(container);
        useTransaction.set(false);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void z8_exec() {
        super.z8_exec();

        if (getUser().securityGroup() != SecurityGroup.Administrators) {
            print("You must be a member of Administrators security group to perform this action.");
            reportProgress(100);
            return;
        }
        
        Scheduler.stop();

        reportProgress(0);

        Logger logger = new Logger();

        try {
            Connection connection = ConnectionManager.get();

            Collection<Table.CLASS<? extends Table>> tables = getTables();
            Collection<Desktop.CLASS<? extends Desktop>> entries = (Collection) Runtime.instance().entries();
            Collection<Procedure.CLASS<? extends Procedure>> jobs = (Collection) Runtime.instance().jobs();

            try {
                DBGenerator generator = new DBGenerator(connection);
                generator.run(tables, entries, jobs, true, logger, true);
            } catch (Throwable e) {
                logger.error(e);
            }
        } catch (Throwable e) {
            logger.error(e);
        }

        Scheduler.start();

        reportProgress(100);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Collection<Table.CLASS<? extends Table>> getTables() {
        Map<String, Table.CLASS<? extends Table>> nameToClass = new HashMap<String, Table.CLASS<? extends Table>>();
        Collection<Table.CLASS<? extends Table>> tables = (Collection) Runtime.instance().tables();

        for (Table.CLASS<? extends Table> cls : tables) {
            String name = cls.name();
            Table.CLASS<? extends Table> c = nameToClass.get(name);

            if (c == null || c.hasAttribute(IObject.Native)) {
                nameToClass.put(name, cls);
            }
        }

        return nameToClass.values();
    }

    private class Logger implements ILogger {
        @Override
        public void error(Throwable exception, String message) {
            Trace.logError(message, exception);
            print(message);
        }

        @Override
        public void error(Throwable exception) {
            error(exception, ErrorUtils.getMessage(exception));
        }

        @Override
        public void message(String message) {
            Trace.logEvent(message);
            print(message);
        }

        @Override
        public void progress(int percentDone) {
            reportProgress(percentDone);
        }
    }

}
