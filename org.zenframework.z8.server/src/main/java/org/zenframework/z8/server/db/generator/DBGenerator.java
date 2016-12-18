package org.zenframework.z8.server.db.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.job.scheduler.Scheduler;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.utils.ErrorUtils;

public class DBGenerator {
	public static final String SchemaGenerateLock = "SchemaGenerate";

	private Connection connection;
	private ILogger logger;
	private Collection<Table.CLASS<Table>> tables;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public DBGenerator(ILogger logger) {
		this.connection = ConnectionManager.get();
		this.logger = logger;
		tables = (Collection)Runtime.instance().tables();
	}

	public void run() {
		try {
			Scheduler.stop();
			logger.info(Resources.get("Generator.schedulerStopped"));

			ApplicationServer.disableEvents();

			run(DataSchema.getTables(connection, "%"));
		} catch(Throwable e) {
			logger.error(e);
		} finally {
			ApplicationServer.enableEvents();

			Scheduler.start();
			logger.info(Resources.get("Generator.schedulerStarted"));
		}
	}

	private void run(Map<String, TableDescription> existingTables) {
		List<TableGenerator> generators = getTableGenerators(existingTables);

		logger.progress(0);

		int total = 5 * generators.size();
		float progress = 0.0f;

		fireBeforeDbGenerated();

		for(TableGenerator generator : generators) {
			generator.dropAllKeys(connection);
			logger.progress(Math.round(++progress / total * 100));
		}

		for(TableGenerator generator : generators) {
			generator.create(connection);
			logger.progress(Math.round(++progress / total * 100));
		}

		for(TableGenerator generator : generators) {
			generator.createRecords();
			logger.progress(Math.round(++progress / total * 100));
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

		fireAfterDbGenerated();

		String version = Runtime.version();

		logger.info("Control sum: " + version);
		logger.progress(100);
	}

	private List<TableGenerator> getTableGenerators(Map<String, TableDescription> existingTables) {
		List<TableGenerator> generators = new ArrayList<TableGenerator>();

		for(Table.CLASS<? extends Table> table : tables) {
			GeneratorAction action;

			TableDescription description = existingTables.get(table.name());

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

	private static void fireBeforeDbGenerated() {
		for(Listener listener : listeners)
			listener.beforeStart();
	}

	private static void fireAfterDbGenerated() {
		for(Listener listener : listeners)
			listener.afterFinish();
	}

	static private Collection<Listener> listeners = new ArrayList<Listener>();

	static public interface Listener {
		public void beforeStart();
		public void afterFinish();
	}

	static public void addListener(Listener listener) {
		listeners.add(listener);
	}

	static public void removeListener(Listener listener) {
		listeners.remove(listener);
	}
}
