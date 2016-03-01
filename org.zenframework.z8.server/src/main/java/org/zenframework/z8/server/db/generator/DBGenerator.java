package org.zenframework.z8.server.db.generator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.form.Desktop;
import org.zenframework.z8.server.base.simple.Activator;
import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.utils.ErrorUtils;

public class DBGenerator {
    public static final String SchemaGenerateLock = "SchemaGenerate";

    private Connection connection;

    public DBGenerator(Connection connection) {
        this.connection = connection;
    }

    public void run(Collection<Table.CLASS<? extends Table>> tables, Collection<Desktop.CLASS<? extends Desktop>> entries,
            Collection<Procedure.CLASS<? extends Procedure>> jobs, boolean doDropTables, ILogger logger,
            boolean doCreateEntries) {
        logger.progress(0);

        ApplicationServer.disableEvents();

        try {
            run(tables, DataSchema.getTables(connection, "%"), doDropTables, logger, entries);
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            ApplicationServer.enableEvents();
        }

        logger.progress(100);
    }

    public void run(Collection<Table.CLASS<? extends Table>> tables, Map<String, TableDescription> existingTables,
            boolean doDropTable, ILogger logger, Collection<Desktop.CLASS<? extends Desktop>> entries) {
        List<TableGenerator> generators = getTableGenerators(tables, existingTables, logger);

        int total = 6 * generators.size();
        float progress = 0.0f;

        fireBeforeDbGenerated();

        for (TableGenerator generator : generators) {
            generator.create(connection);
            progress++;
            logger.progress(Math.round(progress / total * 100));
        }

        for (TableGenerator generator : generators) {
            generator.createRecords();
            progress++;
            logger.progress(Math.round(progress / total * 100));
        }

        for (TableGenerator generator : generators) {
            generator.createPrimaryKey();
            progress++;
            logger.progress(Math.round(progress / total * 100));
        }

        try {
            new EntriesGenerator(connection).run(entries, logger);
        } catch (Throwable e) {
            logger.error(e, ErrorUtils.getMessage(e));
        }

        try {
            new JobGenerator().run(logger);
        } catch (Throwable e) {
            logger.error(e, ErrorUtils.getMessage(e));
        }

        for (TableGenerator generator : generators) {
            generator.createForeignKeys();
            progress++;
            logger.progress(Math.round(progress / total * 100));
        }

        String version = Runtime.version();
        Properties.setProperty(ServerRuntime.DbSchemeControlSumProperty, version);
        logger.message("Control sum: " + version);

        fireAfterDbGenerated();

        logger.progress(100);
    }

    private List<TableGenerator> getTableGenerators(Collection<Table.CLASS<? extends Table>> tables,
            Map<String, TableDescription> existingTables, ILogger logger) {
        List<TableGenerator> generators = new ArrayList<TableGenerator>();

        for (Table.CLASS<? extends Table> table : tables) {
            GeneratorAction action;

            TableDescription description = existingTables.get(table.name());

            if (description != null && description.isView()) {
                continue;
            }

            if (description != null) {
                action = GeneratorAction.Alter;
                generators.add(new TableGenerator(table, action, description, logger));
            } else {
                action = GeneratorAction.Create;
                generators.add(new TableGenerator(table, action, new TableDescription(table.name(), false), logger));
            }
        }

        return generators;
    }

    private static void fireBeforeDbGenerated() {
        for (Activator.CLASS<? extends Activator> activator : Runtime.instance().activators()) {
            activator.get().beforeDbGenerated();
        }
    }

    private static void fireAfterDbGenerated() {
        for (Activator.CLASS<? extends Activator> activator : Runtime.instance().activators()) {
            activator.get().afterDbGenerated();
        }
    }

}
