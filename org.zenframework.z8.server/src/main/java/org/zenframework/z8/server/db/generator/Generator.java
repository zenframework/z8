package org.zenframework.z8.server.db.generator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.query.RecordLock;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Settings;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.engine.Runtime;
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

		for(TableGenerator generator : generators) {
			generator.dropAllKeys();
			logger.progress(Math.round(++progress / total * 100));
		}

		for(TableGenerator generator : generators) {
			generator.create();
			logger.progress(Math.round(++progress / total * 100));
			ConnectionManager.release();
		}

		for(TableGenerator generator : generators) {
			generator.createRecords();
			logger.progress(Math.round(++progress / total * 100));
			ConnectionManager.release();
		}

		for(TableGenerator generator : generators) {
			generator.createPrimaryKey();
			logger.progress(Math.round(++progress / total * 100));
		}

		try {
			new EntriesGenerator(logger).run();
		} catch(Throwable e) {
			logger.error(e, ErrorUtils.getMessage(e));
		}

		try {
			new JobGenerator(logger).run();
		} catch(Throwable e) {
			logger.error(e, ErrorUtils.getMessage(e));
		}

		try {
			new AccessRightsGenerator(logger).run();
		} catch(Throwable e) {
			logger.error(e, ErrorUtils.getMessage(e));
		}

		for(TableGenerator generator : generators) {
			try {
				generator.createForeignKeys();
			} catch(Throwable e) {
				logger.error(e, ErrorUtils.getMessage(e));
			}
			logger.progress(Math.round(++progress / total * 100));
		}

		String version = Runtime.version();
		Settings.save(Settings.Version, guid.Null, "Version", "Schema version", version, RecordLock.Full.getInt(), true);

		logger.info("Control sum: " + version);
		logger.progress(100);
	}

	private List<TableGenerator> getTableGenerators(Map<String, TableDescription> existingTables) {
		List<TableGenerator> generators = new ArrayList<TableGenerator>();

		for(Table.CLASS<? extends Table> table : tables) {
			GeneratorAction action;

			DatabaseVendor vendor = ConnectionManager.get().getVendor();
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
}
