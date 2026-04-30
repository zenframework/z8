package org.zenframework.z8.server.db.generator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Settings;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.engine.Version;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.utils.ErrorUtils;

public class Generator {
	public static final String SchemaGenerateLock = "SchemaGenerate";

	private ILogger logger;
	private Collection<Table.CLASS<Table>> tables;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Generator(ILogger logger) {
		this.logger = logger;
		tables = (Collection)Runtime.instance().tables();
	}

	public void run() throws SQLException {
		run(DataSchema.getTables("%"));
	}

	private void run(Map<String, TableDescription> existingTables) {
		List<TableGenerator> generators = getTableGenerators(existingTables);

		logger.progress(0);

		int total = 5 * generators.size();
		float progress = 0.0f;

		debug("drop keys");

		for(TableGenerator generator : generators) {
			generator.dropAllKeys();
			logger.progress(Math.round(++progress / total * 100));
		}

		debug("generate tables");

		for(TableGenerator generator : generators) {
			String name = generator.name();
			int dbControlSum = generator.getTableDescription().controlSum();
			int clsControlSum = generator.table().controlSum();

			if (dbControlSum != clsControlSum) {
				debug(name + " control sum " + dbControlSum + " != " + clsControlSum + ", generating");
				debug(name + "(DB)  " + generator.getTableDescription().controlData());
				debug(name + "(CLS) " + generator.table().controlData());
				generator.create();
			} else {
				debug(name + " control sum unchanged, skipped");
			}

			logger.progress(Math.round(++progress / total * 100));
			ConnectionManager.release();
		}

		debug("create records");

		for(TableGenerator generator : generators) {
			generator.createRecords();
			logger.progress(Math.round(++progress / total * 100));
			ConnectionManager.release();
		}

		debug("create primary keys");

		for(TableGenerator generator : generators) {
			generator.createPrimaryKey();
			logger.progress(Math.round(++progress / total * 100));
		}

		debug("create entries");

		try {
			new EntriesGenerator(logger).run();
		} catch(Throwable e) {
			logger.error(e, ErrorUtils.getMessage(e));
		}

		debug("Create jobs");

		try {
			new JobGenerator(logger).run();
		} catch(Throwable e) {
			logger.error(e, ErrorUtils.getMessage(e));
		}

		debug("create access rights");

		try {
			new AccessRightsGenerator(logger).run();
		} catch(Throwable e) {
			logger.error(e, ErrorUtils.getMessage(e));
		}

		debug("create foreign keys");

		for(TableGenerator generator : generators) {
			try {
				generator.createForeignKeys();
			} catch(Throwable e) {
				logger.error(e, ErrorUtils.getMessage(e));
			}
			logger.progress(Math.round(++progress / total * 100));
		}

		Version version = Runtime.version();
		Settings.save(Settings.Version, guid.Null, "Version", "Schema version", version.getVersion(), 0, true);
		Settings.save(Settings.VersionDetails, guid.Null, "Version details", "Schema version details", version.getDetails().toString(), 0, true);

		logger.info("Control sum: " + version.getVersion());
		logger.progress(100);
	}

	private List<TableGenerator> getTableGenerators(Map<String, TableDescription> existingTables) {
		List<TableGenerator> generators = new ArrayList<TableGenerator>();

		for(Table.CLASS<? extends Table> table : tables) {
			GeneratorAction action;

			DatabaseVendor vendor = ConnectionManager.get().vendor();
			TableDescription description = existingTables.get(vendor.sqlName(table.name()));

			if(description != null && description.isView())
				continue;

			if(description != null) {
				action = GeneratorAction.Alter;
				generators.add(new TableGenerator(table, action, description, logger));
			} else {
				action = GeneratorAction.Create;
				generators.add(new TableGenerator(table, action, new TableDescription(table.name(), false), logger));
			}
		}

		return generators;
	}

	private static void debug(String message) {
		Trace.debug("Generator: " + message);
	}
}
