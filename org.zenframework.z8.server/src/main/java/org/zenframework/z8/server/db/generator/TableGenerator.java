package org.zenframework.z8.server.db.generator;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Roles;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.CountingSelect;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.DmlStatement;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.dialect.DatabaseDialect;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.functions.If;
import org.zenframework.z8.server.db.sql.functions.IsNull;
import org.zenframework.z8.server.db.sql.functions.conversion.ToBytes;
import org.zenframework.z8.server.db.sql.functions.conversion.ToString;
import org.zenframework.z8.server.engine.IDatabase;
import org.zenframework.z8.server.exceptions.db.ObjectAlreadyExistException;
import org.zenframework.z8.server.exceptions.db.ObjectNotFoundException;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.security.BuiltinUsers;
import org.zenframework.z8.server.security.Role;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.sql.sql_integer;
import org.zenframework.z8.server.utils.ErrorUtils;

public class TableGenerator {
	private final IDatabase database;
	private final Table.CLASS<? extends Table> tableClass;
	private final GeneratorAction action;
	private final ILogger logger;
	private final TableDescription dbTable;
	private Table table = null;

	public TableGenerator(IDatabase database, Table.CLASS<? extends Table> tableClass, GeneratorAction action, TableDescription dbTable, ILogger logger) {
		this.database = database;
		this.tableClass = tableClass;
		this.action = action;
		this.dbTable = dbTable;
		this.logger = logger;
	}

	public GeneratorAction getAction() {
		return action;
	}

	public Table table() {
		if (table == null)
			table = (Table) tableClass.newInstance();
		return table;
	}

	public TableDescription dbTable() {
		return dbTable;
	}

	private Connection connection() {
		return ConnectionManager.get();
	}

	public void create() {
		try {
			switch(action) {
			case Skip:
				break;
			case Create:
				createTable(table().name());
				break;
			case Recreate:
				recreateTable();
				break;
			default:
				throw new UnsupportedOperationException();
			}
		} catch(SQLException e) {
			logger.error(e, Resources.format("Generator.createTableError", table().displayName(), "[" + database.schema() + "]." + table().name(), ErrorUtils.getMessage(e)));
		} finally {
			table = null;
		}
	}

	public void createPrimaryKey() {
		Field primaryKeyField = table().primaryKey();

		if (primaryKeyField == null)
			return;

		try {
			DmlStatement.execute(database.dialect().getCreatePrimaryKey(database, new PrimaryKey(null, table().name(), primaryKeyField.name())));
		} catch(ObjectAlreadyExistException e) {
		} catch(SQLException e) {
			logger.error(e, Resources.format("Generator.createUniqueIndexError", table().displayName(), "[" + database.schema() + "]." + table().name(), ErrorUtils.getMessage(e)));
		} finally {
			table = null;
		}
	}

	public void createRecords() {
		createNullRecord();

		for (Map<IField, primary> record : table().getStaticRecords())
			createStaticRecord(record);
	}

	public void createIndexes() {
		Table table = table();
		int i = 0;

		for (IField field : table.getIndexedFields()) {
			try {
				Index index = new Index(table.name(), field, i++);
				DmlStatement.execute(database.dialect().getCreateIndex(database, index));
				debug("create index " + index);
			} catch(SQLException e) {
				logger.error(e, Resources.format("Generator.createIndexError", field.displayName(), table.displayName(), table.name(), ErrorUtils.getMessage(e)));
			}
		}
	}

	public void dropIndexes() {
		if (action != GeneratorAction.Recreate)
			return;

		for (Index index : dbTable.getIndexes().values()) {
			try {
				DmlStatement.execute(database.dialect().getDropIndex(database, index.getTableName(), index.getName()));
				debug("drop index " + index);
			} catch(ObjectNotFoundException e) {
			} catch(SQLException e) {
				logger.error(e, Resources.format("Generator.dropIndexError", index.getTableName(), index.getName(), ErrorUtils.getMessage(e)));
			}
		}
	}

	public void optimizeTable() {
		String tableName = table().name();

		try {
			String sql = database.dialect().getOptimizeTable(database, tableName);
			if (sql != null) {
				DmlStatement.execute(sql);
				debug("Optimized: " + tableName);
			}
		} catch(SQLException e) {
			logger.error(e, Resources.format("Generator.optimizeTableError", tableName, ErrorUtils.getMessage(e)));
		}
	}

	private void recreateTable() throws SQLException {
		String tableName = table().name();
		String tmpName = Integer.toString(Math.abs(tableName.hashCode()));

		Connection connection = connection();

		try {
			connection.beginTransaction();

			createTable(tmpName);

			if (connection.vendor() == DatabaseVendor.H2)
				connection.flush();

			moveData(tmpName);
			dropTable(tableName);
			renameTable(tmpName, tableName);

			connection.commit();
		} catch (Throwable e) {
			connection.rollback();
			throw e instanceof SQLException ? (SQLException) e : new SQLException(e);
		}
	}

	private void createTable(String name) throws SQLException {
		DmlStatement.execute(database.dialect().getCreateTable(database, name, table().getPrimaryFields()));
		debug("create table '" + name + "'");
	}

	private void renameTable(String oldTableName, String newTableName) throws SQLException {
		DmlStatement.execute(database.dialect().getRenameTable(database, oldTableName, newTableName));
		debug("rename table '" + oldTableName + "' to '" + newTableName + "'");
	}

	private void dropTable(String tableName) throws SQLException {
		DmlStatement.execute(database.dialect().getDropTable(database, tableName));
		debug("drop table '" + tableName + "'");
	}

	private void moveData(String dstTableName) throws SQLException {
		String targetFields = "";
		String sourceFields = "";

		DatabaseVendor vendor = database.vendor();
		DatabaseDialect dialect = database.dialect();

		boolean postgres = vendor == DatabaseVendor.Postgres || vendor == DatabaseVendor.H2;
		boolean oracle = vendor == DatabaseVendor.Oracle;

		FormatOptions options = new FormatOptions();
		options.disableAggregation();

		Map<String, Column> dbFields = dbTable.getColumns();

		for (Field field : table().getPrimaryFields()) {
			String name = field.name();

			Column dbField = dbFields.get(name);

			if (dbField == null)
				continue;

			FieldType type = field.type();

			targetFields += (targetFields.isEmpty() ? "" : ", ") + dialect.quote(name);

			if (type == FieldType.Guid)
				name = new If(new IsNull(field), guid.Null.sql_guid(), new SqlField(field)).format(vendor, options);
			else if (postgres && dbField.getType().equals("uuid") && type == FieldType.Integer)
				name = "null";
			else if (postgres && dbField.getType().startsWith("character") && type == FieldType.Text)
				name = new ToBytes(field).format(vendor, options);
			else if (postgres && dbField.getType().startsWith("bytea") && type == FieldType.String)
				name = new ToString(field).format(vendor, options);
			else if (oracle && (dbField.getType().startsWith("BLOB") || dbField.getType().startsWith("NCLOB")))
				name = "null";
			else if (postgres && dbField.getType().startsWith("timestamp") && (type == FieldType.Date || type == FieldType.Datetime)) {
				SqlToken condition = new Rel(field, Operation.LT, new SqlStringToken("'1900-01-01 00:00:00'", FieldType.Datetime));
				SqlToken yes = new sql_integer(date.UtcMin);
				SqlToken no = new SqlStringToken("extract(epoch from " + new SqlField(field).format(vendor, options) + ") * 1000", FieldType.Integer);
				name = new If(condition, yes, no).format(vendor, options);
			} else
				name = dialect.quote(name);

			sourceFields += (sourceFields.isEmpty() ? "" : ", ") + name;
		}

		options.enableAggregation();

		if (targetFields.isEmpty())
			return;

		String sql = "insert into " + database.tableName(dstTableName) + " (" + targetFields + ")";
		sql += " select " + sourceFields + " from " + database.tableName(table().name()) + (vendor == DatabaseVendor.SqlServer ? " as " : "") + table().getAlias();

		DmlStatement.execute(sql);

		debug("move data from '" + table().name() + "' to '" + dstTableName + "'");
	}

	private void createNullRecord() {
		Table table = table();

		try {
			if (!findRecord(guid.Null))
				table.create(guid.Null);
		} catch(Throwable e) {
			logger.error(e, Resources.format("Generator.insertRecordsError", guid.Null.toString(), table.displayName(), table.name(), ErrorUtils.getMessage(e)));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createStaticRecord(Map<IField, primary> record) {
		Table table = table();
		Field primaryKey = table.primaryKey();
		guid recordId = (guid)record.get(primaryKey);
		Collection<Field> fields = (Collection)record.keySet();

		try {
			if (!findRecord(recordId)) {
				for (Field field : fields)
					field.set(record.get(field));
				table.create(recordId);
				return;
			}

			boolean isUsers = table instanceof Users && (BuiltinUsers.System.guid().equals(recordId) || 
					BuiltinUsers.Administrator.guid().equals(recordId));

			boolean isRoles = table instanceof Roles && (Role.Administrator.equals(recordId) || 
					Role.User.equals(recordId) || Role.Guest.equals(recordId));

			Users users = isUsers ? (Users) table : null;
			Roles roles = isRoles ? (Roles) table : null;

			for (Field field : fields) {
				if (isUsers && (field == users.password.get() || field == users.name.get() || field == users.description.get()))
					continue;

				if (isRoles && (field == roles.name.get() || field == roles.description.get() || 
						field == roles.read.get() || field == roles.write.get() || field == roles.create.get() ||
						field == roles.copy.get() || field == roles.destroy.get() || field == roles.execute.get()))
					continue;

				primary value = record.get(field);
				field.set(value);
			}

			table.update(recordId);
		} catch (SQLException e) {
			logger.error(e, Resources.format("Generator.insertRecordsError", recordId.toString(), table.displayName(), table.name(), ErrorUtils.getMessage(e)));
		}
	}

	private boolean findRecord(guid recordId) throws SQLException {
		CountingSelect select = new CountingSelect();

		Field primaryKey = table().primaryKey();
		SqlToken where = new Equ(primaryKey, recordId);

		select.setWhere(where);
		select.setRootQuery(table());

		return select.count() != 0;
	}

	private void debug(String message) {
		Trace.debug("'" + tableClass.name() + "' generator: " + message);
	}
}
