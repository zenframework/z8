package org.zenframework.z8.server.db.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Settings;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.IDatabase;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.engine.Version;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.guid;

public class Generator {
	public static final String SchemaGenerateLock = "SchemaGenerate";

	private IDatabase database;
	private Collection<Table.CLASS<Table>> tables;
	private ILogger logger;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Generator(IDatabase database, ILogger logger) {
		this.database = database;
		this.tables = (Collection) Runtime.instance().tables();
		this.logger = logger;
	}

	public void run() {
		IDatabase database = ConnectionManager.database();
		List<TableGenerator> allTables = getTableGenerators(database, new DataSchema().initialize());
		List<TableGenerator> changedTables = filterUnchanged(allTables);
		List<ForeignKeyGenerator> foreignKeys = getChangedForeignKeyGenerators(database, changedTables);

		logger.progress(0);

		int total = 5 * changedTables.size() + allTables.size() + 2 * foreignKeys.size() + 3;
		int progress = 0;

		debug("drop foreign keys");

		for (ForeignKeyGenerator generator : foreignKeys) {
			generator.drop();
			logger.progress(++progress * 100 / total);
		}

		debug("drop indexes");

		for (TableGenerator generator : changedTables) {
			generator.dropIndexes();
			logger.progress(++progress * 100 / total);
		}

		debug("generate tables");

		for (TableGenerator generator : changedTables) {
			generator.create();
			logger.progress(++progress * 100 / total);
			ConnectionManager.release();
		}

		debug("create records");

		for (TableGenerator generator : allTables) {
			generator.createRecords();
			logger.progress(++progress * 100 / total);
			ConnectionManager.release();
		}

		debug("create primary keys");

		for (TableGenerator generator : changedTables) {
			generator.createPrimaryKey();
			logger.progress(++progress * 100 / total);
		}

		debug("create entries");

		new EntriesGenerator(logger).run();
		logger.progress(++progress * 100 / total);

		debug("create jobs");

		new JobGenerator(logger).run();
		logger.progress(++progress * 100 / total);

		debug("create access rights");

		new AccessRightsGenerator(logger).run();
		logger.progress(++progress * 100 / total);

		debug("create indexes");

		for (TableGenerator generator : changedTables) {
			generator.createIndexes();
			logger.progress(++progress * 100 / total);
		}

		debug("create foreign keys");

		for (ForeignKeyGenerator generator : foreignKeys) {
			generator.create();
			logger.progress(++progress * 100 / total);
		}

		debug("optimize tables");

		for(TableGenerator generator : changedTables) {
			generator.optimizeTable();
			logger.progress(++progress * 100 / total);
		}

		Version version = Runtime.version();
		Settings.save(Settings.Version, guid.Null, "Version", "Schema version", version.getVersion(), 0, true);
		Settings.save(Settings.VersionDetails, guid.Null, "Version details", "Schema version details", version.getDetails().toString(), 0, true);

		logger.info("Control sum: " + version.getVersion());
		logger.progress(100);
	}

	private List<TableGenerator> getTableGenerators(IDatabase database, DataSchema dataSchema) {
		Map<String, TableDescription> existingTables = dataSchema.getTables();
		List<TableGenerator> generators = new ArrayList<TableGenerator>();

		for (Table.CLASS<? extends Table> tableClass : tables) {
			TableDescription description = existingTables.get(database.dialect().formatSqlName(tableClass.name()));
			Table table = tableClass.newInstance();
			String name = tableClass.name();

			if (description == null) {
				generators.add(new TableGenerator(database, tableClass, GeneratorAction.Create, new TableDescription(name), logger));
				debug(name + " doesn't exist, creating");
			} else if (description.controlSum() != table.controlSum() && !table.skipRecreation()) {
				generators.add(new TableGenerator(database, tableClass, GeneratorAction.Recreate, description, logger));
				debug(name + " control sum " + description.controlSum() + " != " + table.controlSum() + ", recreating");
				//debug(name + "(DB)  " + description.controlData());
				//debug(name + "(CLS) " + table.controlData());
			} else {
				generators.add(new TableGenerator(database, tableClass, GeneratorAction.Skip, description, logger));
				//debug(name + " skipped");
			}
		}

		return generators;
	}

	private List<ForeignKeyGenerator> getChangedForeignKeyGenerators(IDatabase database, Collection<TableGenerator> tables) {
		Map<ForeignKey, ForeignKeyGenerator> generators = new HashMap<ForeignKey, ForeignKeyGenerator>();

		for (TableGenerator generator : tables) {
			if (generator.getAction() == GeneratorAction.Create || generator.getAction() == GeneratorAction.Recreate)
				collectForeignKeyGenerators(generator, generators);
		}

		for (TableGenerator generator : tables) {
			if (generator.getAction() == GeneratorAction.Create || generator.getAction() == GeneratorAction.Recreate)
				collectRefererGenerators(generator, generators);
		}

		return new ArrayList<ForeignKeyGenerator>(generators.values());
	}

	private void collectForeignKeyGenerators(TableGenerator table, Map<ForeignKey, ForeignKeyGenerator> generators) {
		Set<ForeignKey> existingForeignKeys = new HashSet<ForeignKey>(table.dbTable().getForeignKeys());
		int index = 0;

		for (IForeignKey link : table.table().getForeignKeys()) {
			ForeignKey foreignKey = new ForeignKey(table.table().name(), link, index++);
			GeneratorAction action = existingForeignKeys.remove(foreignKey) ? GeneratorAction.Recreate : GeneratorAction.Create;
			generators.put(foreignKey, new ForeignKeyGenerator(database, foreignKey, action, logger));
		}

		for (ForeignKey foreignKey : existingForeignKeys)
			generators.put(foreignKey, new ForeignKeyGenerator(database, foreignKey, GeneratorAction.Drop, logger));
	}

	private void collectRefererGenerators(TableGenerator table, Map<ForeignKey, ForeignKeyGenerator> generators) {
		for (ForeignKey foreignKey : table.dbTable().getReferers()) {
			ForeignKeyGenerator generator = generators.get(foreignKey);
			if (generator == null)
				generators.put(foreignKey, new ForeignKeyGenerator(database, foreignKey, GeneratorAction.Recreate, logger));
		}
	}

	private static List<TableGenerator> filterUnchanged(List<TableGenerator> generators) {
		List<TableGenerator> filtered = new ArrayList<TableGenerator>(generators.size());

		for (TableGenerator generator : generators)
			if (generator.getAction() != GeneratorAction.Skip)
				filtered.add(generator);

		return filtered;
	}

	private static void debug(String message) {
		Trace.debug("Generator: " + message);
	}
}
