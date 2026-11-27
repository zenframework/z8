package org.zenframework.z8.server.db.generator;

import java.sql.SQLException;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.db.DmlStatement;
import org.zenframework.z8.server.engine.IDatabase;
import org.zenframework.z8.server.exceptions.db.ObjectNotFoundException;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.utils.ErrorUtils;

public class IndexGenerator {
	private final IDatabase database;
	private final Index index;
	private final GeneratorAction action;
	private final ILogger logger;
	private final Table table;

	public IndexGenerator(IDatabase database, Index index, GeneratorAction action, ILogger logger) {
		this(database, index, action, logger, null);
	}

	public IndexGenerator(IDatabase database, Index index, GeneratorAction action, ILogger logger, Table table) {
		this.database = database;
		this.index = index;
		this.action = action;
		this.logger = logger;
		this.table = table;
	}

	public void drop() {
		if (action != GeneratorAction.Drop)
			return;

		try {
			DmlStatement.execute(database.dialect().getDropIndex(database, index.getTableName(), index.getName()));
			debug("drop index " + index);
		} catch(ObjectNotFoundException e) {
		} catch(SQLException e) {
			logger.error(e, Resources.format("Generator.dropIndexError", index.getTableName(), index.getName(), ErrorUtils.getMessage(e)));
		}
	}

	public void create() {
		if (action != GeneratorAction.Create && action != GeneratorAction.Recreate)
			return;

		try {
			String sql = action == GeneratorAction.Recreate ? index.getDefinition() : database.dialect().getCreateIndex(database, index);
			DmlStatement.execute(sql);
			debug("create index " + index);
		} catch(SQLException | RuntimeException e) {
			if (action == GeneratorAction.Recreate)
				logger.warning(Resources.format("Generator.restoreIndexWarning", index.getName(), index.getTableName(), ErrorUtils.getMessage(e)));
			else
				logger.error(e, Resources.format("Generator.createIndexError", index.getName(), table.displayName(), table.name(), ErrorUtils.getMessage(e)));
		}
	}

	private void debug(String message) {
		Trace.debug("'" + index.getTableName() + "' generator: " + message);
	}
}
