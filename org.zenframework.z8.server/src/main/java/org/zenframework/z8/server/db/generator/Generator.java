package org.zenframework.z8.server.db.generator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private static final String IndexNamePattern = "(?:" + Pattern.quote(Index.IndexPrefix) + "|" + Pattern.quote(Index.UniqueIndexPrefix) + ")([0-9]+)";

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
		List<ForeignKeyGenerator> foreignKeys = getForeignKeyGenerators(database, allTables);
		List<IndexGenerator> indexes = getIndexGenerators(allTables);

		logger.progress(0);

		int total = 3 * changedTables.size() + allTables.size() + 2 * foreignKeys.size() + 2 * indexes.size() + 3;
		int progress = 0;

		debug("drop foreign keys");

		for (ForeignKeyGenerator generator : foreignKeys) {
			generator.drop();
			logger.progress(++progress * 100 / total);
		}

		debug("drop indexes");

		for (IndexGenerator generator : indexes) {
			generator.drop();
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

		for (IndexGenerator generator : indexes) {
			generator.create();
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

	private List<ForeignKeyGenerator> getForeignKeyGenerators(IDatabase database, Collection<TableGenerator> tables) {
		Map<ForeignKey, ForeignKeyGenerator> generators = new HashMap<ForeignKey, ForeignKeyGenerator>();

		for (TableGenerator generator : tables)
			collectForeignKeyGenerators(generator, generators);

		for (TableGenerator generator : tables)
			collectRefererGenerators(generator, generators);

		return new ArrayList<ForeignKeyGenerator>(generators.values());
	}

	private void collectForeignKeyGenerators(TableGenerator table, Map<ForeignKey, ForeignKeyGenerator> generators) {
		Map<ForeignKey, String> existingForeignKeys = ForeignKey.toNamesMap(table.dbTable().getForeignKeys());
		GeneratorAction tableAction = table.getAction();
		int index = 0;

		if (tableAction == GeneratorAction.Skip && tableAction != GeneratorAction.Drop) {
			GeneratorAction keyAction = tableAction == GeneratorAction.Drop ? GeneratorAction.Drop : GeneratorAction.Recreate;
			for (Map.Entry<ForeignKey, String> entry : existingForeignKeys.entrySet())
				generators.put(entry.getKey(), new ForeignKeyGenerator(database, entry.getKey(), keyAction, logger));
			return;
		}

		// Create or recreate

		for (IForeignKey link : table.table().getForeignKeys()) {
			String tableName = table.table().name();
			ForeignKey foreignKey = new ForeignKey(link.getReferencedTable().name(), link.getReferer().name(), tableName, link.getFieldDescriptor().name(), "FK" + index++ + "_" + tableName);
			String existingName = existingForeignKeys.remove(foreignKey);
			GeneratorAction action = existingName != null ? GeneratorAction.Recreate : GeneratorAction.Create;
			generators.put(foreignKey, new ForeignKeyGenerator(database, foreignKey.setOldName(existingName), action, logger));
		}

		for (ForeignKey foreignKey : existingForeignKeys.keySet())
			generators.put(foreignKey, new ForeignKeyGenerator(database, foreignKey, GeneratorAction.Drop, logger));
	}

	private void collectRefererGenerators(TableGenerator table, Map<ForeignKey, ForeignKeyGenerator> generators) {
		for (ForeignKey foreignKey : table.dbTable().getReferers())
			if (!generators.containsKey(foreignKey))
				generators.put(foreignKey, new ForeignKeyGenerator(database, foreignKey,
						table.getAction() == GeneratorAction.Drop ? GeneratorAction.Drop : GeneratorAction.Recreate, logger));
	}

	private List<IndexGenerator> getIndexGenerators(Collection<TableGenerator> tables) {
		List<IndexGenerator> generators = new ArrayList<IndexGenerator>();
		try {
			for (TableGenerator table : tables)
				collectIndexGenerators(table, generators);
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
		return generators;
	}

	private void collectIndexGenerators(TableGenerator table, List<IndexGenerator> generators) throws SQLException {
		Table model = table.table();
		Collection<Index> dbIndexes = table.dbTable().getIndexes().values();
		List<Index> modelIndexes = new ArrayList<Index>(model.getIndexes());

		switch (table.getAction()) {
		case Skip: {
			List<Index> generatedDbIndexes = new ArrayList<Index>();
			Set<String> names = new HashSet<String>();
			for (Index index : dbIndexes) {
				names.add(index.getName());
				if (isGeneratedIndexName(index.getName(), model.name()))
					generatedDbIndexes.add(index);
			}

			List<Index> indexes = new ArrayList<Index>(modelIndexes);
			indexes.addAll(generatedDbIndexes);
			DataSchema.normalizeIndexValues(database, model.name(), indexes);
			Set<Index> generatedDbIndexSet = new HashSet<Index>(generatedDbIndexes);
			Set<Index> modelIndexSet = new HashSet<Index>(modelIndexes);
			int free = modelIndexes.size();

			for (Index index : modelIndexes) {
				if (generatedDbIndexSet.contains(index))
					continue;
				String name = index.getName();
				while (isNameTaken(names, name))
					name = Index.createName(model.name(), index.isUnique(), free++);
				index.setName(name);
				names.add(database.dialect().normalizeIdentifier(name));
				generators.add(new IndexGenerator(database, index, GeneratorAction.Create, logger, model));
			}

			for (Index index : generatedDbIndexes) {
				if (!modelIndexSet.contains(index))
					generators.add(new IndexGenerator(database, index, GeneratorAction.Drop, logger));
			}
			break;
		}
		case Recreate:
			for (Index index : dbIndexes) {
				generators.add(new IndexGenerator(database, index, GeneratorAction.Drop, logger));
				if (!isGeneratedIndexName(index.getName(), model.name()))
					generators.add(new IndexGenerator(database, index, GeneratorAction.Recreate, logger));
			}
		case Create:
			for (Index index : modelIndexes)
				generators.add(new IndexGenerator(database, index, GeneratorAction.Create, logger, model));
			break;
		default:
			break;
		}
	}

	private boolean isGeneratedIndexName(String name, String tableName) {
		if (name.matches(IndexNamePattern + Pattern.quote(tableName)))
			return true;

		Matcher prefix = Pattern.compile(IndexNamePattern).matcher(name);
		if (!prefix.lookingAt())
			return false;

		for (int end = prefix.start(1) + 1; end <= prefix.end(); end++) {
			String fullName = name.substring(0, end) + tableName;
			if (name.equals(database.dialect().normalizeIdentifier(fullName)))
				return true;
		}
		return false;
	}

	private boolean isNameTaken(Collection<String> names, String name) {
		return names.contains(database.dialect().normalizeIdentifier(name));
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
