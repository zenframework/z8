package org.zenframework.z8.server.db.generator;

import java.sql.SQLException;

import org.zenframework.z8.server.db.DmlStatement;
import org.zenframework.z8.server.engine.IDatabase;
import org.zenframework.z8.server.exceptions.db.ObjectNotFoundException;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.utils.ErrorUtils;

public class ForeignKeyGenerator {
	private final IDatabase database;
	private final ForeignKey foreignKey;
	private final GeneratorAction action;
	private final ILogger logger;

	public ForeignKeyGenerator(IDatabase database, ForeignKey foreignKey, GeneratorAction action, ILogger logger) {
		this.database = database;
		this.foreignKey = foreignKey;
		this.action = action;
		this.logger = logger;
	}

	public void drop() {
		if (action != GeneratorAction.Recreate && action != GeneratorAction.Drop)
			return;

		try {
			DmlStatement.execute(database.dialect().getDropForeignKey(database, foreignKey));
			debug("drop foreign key " + foreignKey);
		} catch(ObjectNotFoundException e) {
		} catch(SQLException e) {
			logger.error(e, Resources.format("Generator.dropForeignKeyError", foreignKey.getTable(), foreignKey.getName(), ErrorUtils.getMessage(e)));
		}
	}

	public void create() {
		if (action != GeneratorAction.Recreate && action != GeneratorAction.Create)
			return;

		try {
			DmlStatement.execute(database.dialect().getCreateForeignKey(database, foreignKey));
			debug("create foreign key " + foreignKey);
		} catch (SQLException e) {
			logger.error(e, Resources.format("Generator.createForeignKeyError", foreignKey.getTable(), foreignKey.getReferenceTable(), ErrorUtils.getMessage(e)));
		}
	}

	public ForeignKey getForeignKey() {
		return foreignKey;
	}

	public GeneratorAction getAction() {
		return action;
	}

	@Override
	public String toString() {
		return foreignKey.toString();
	}

	private void debug(String message) {
		Trace.debug("'" + foreignKey.getName() + "' generator: " + message);
	}
}
