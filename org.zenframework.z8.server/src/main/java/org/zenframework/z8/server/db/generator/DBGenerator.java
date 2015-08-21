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
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.exceptions.UnsupportedParameterException;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.utils.ErrorUtils;

public class DBGenerator {
    public static final String SchemaGenerateLock = "SchemaGenerate";

    private Connection connection;

    public DBGenerator(Connection connection) {
        this.connection = connection;
    }

    public GenerateType check(Collection<Table.CLASS<? extends Table>> tables, Map<String, TableDescription> existingTables,
            ILogger logger) {
        List<TableGenerator> generators = getTableGenerators(tables, existingTables, logger);

        GenerateType genType = new GenerateType();

        for (TableGenerator table : generators)
            switch (table.getAction()) {
            case None:
                break;
            case Create:
                genType.setLite();
                logger.message(Resources.format("Generator.recreatringTable", table.displayName(), table.name()));
                break;
            case Alter:
            case Recreate: {
                GeneratorAction type = table.checkAlter();
                if (type != GeneratorAction.None) {
                    if (type == GeneratorAction.Alter) {
                        genType.setLite();
                    } else if (type == GeneratorAction.Recreate) {
                        genType.setFull();
                    }
                    logger.message(Resources.format("Generator.tableRegeneration",
                            new Object[] { table.displayName(), table.name() }));
                }
                break;
            }

            default:
                throw new UnsupportedParameterException();
            }

        for (String sTableExisting : existingTables.keySet()) {
            boolean bDrop = existingTables.get(sTableExisting).isView();

            for (Table.CLASS<? extends Table> table : tables) {
                if (table.name().equalsIgnoreCase(sTableExisting)) {
                    bDrop = false;
                    break;
                }
            }

            if (bDrop) {
                genType.setOlds();
                logger.message(Resources.format("Generator.droppingTable", new Object[] { sTableExisting }));
            }
        }
        return genType;
    }

    public void run(Collection<Table.CLASS<? extends Table>> tables, Collection<Desktop.CLASS<? extends Desktop>> entries,
            Collection<Procedure.CLASS<? extends Procedure>> jobs, boolean doDropTables, ILogger logger,
            boolean doCreateEntries) {
        logger.progress(0);

        try {
            run(tables, DataSchema.getTables(connection, "%"), doDropTables, logger, entries);
        } catch (SQLException e) {
            logger.error(e);
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
