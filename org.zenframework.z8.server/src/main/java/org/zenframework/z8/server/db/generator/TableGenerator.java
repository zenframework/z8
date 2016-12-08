package org.zenframework.z8.server.db.generator;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.zenframework.z8.server.base.model.sql.CountingSelect;
import org.zenframework.z8.server.base.model.sql.Select;
import org.zenframework.z8.server.base.model.sql.Update;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.base.table.value.BoolExpression;
import org.zenframework.z8.server.base.table.value.DateExpression;
import org.zenframework.z8.server.base.table.value.DateField;
import org.zenframework.z8.server.base.table.value.DatespanExpression;
import org.zenframework.z8.server.base.table.value.DatetimeExpression;
import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.DecimalExpression;
import org.zenframework.z8.server.base.table.value.Expression;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidExpression;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.base.table.value.IntegerExpression;
import org.zenframework.z8.server.base.table.value.StringExpression;
import org.zenframework.z8.server.base.table.value.TextExpression;
import org.zenframework.z8.server.db.BasicSelect;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.Cursor;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.Statement;
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
import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.exceptions.db.ObjectAlreadyExistException;
import org.zenframework.z8.server.exceptions.db.ObjectNotFoundException;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.security.BuiltinUsers;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.sql.sql_bool;
import org.zenframework.z8.server.utils.ErrorUtils;

public class TableGenerator {
	private Table.CLASS<? extends Table> tableClass = null;
	private Table table = null;

	private GeneratorAction action;
	private ILogger logger;
	private Map<String, ColumnDescGen> dbFields = new HashMap<String, ColumnDescGen>();
	private Collection<ColumnDescAlter> dbFieldsAlter;
	private TableDescription dbTable;

	private Connection connection = null;

	public TableGenerator(Table.CLASS<? extends Table> tableClass, GeneratorAction action, TableDescription dbTable, ILogger logger) {
		this.action = action;
		this.dbTable = dbTable;

		this.logger = logger;

		this.tableClass = tableClass;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public String name() {
		return tableClass.name();
	}

	public String displayName() {
		return tableClass.displayName();
	}

	public Table table() {
		if(table == null)
			table = (Table)tableClass.newInstance();
		return table;
	}

	public GeneratorAction getAction() {
		return action;
	}

	private Database database() {
		return connection.database();
	}

	private DatabaseVendor vendor() {
		return connection.vendor();
	}

	public void create(Connection connection) {
		this.connection = connection;

		try {
			if(action == GeneratorAction.Alter)
				action = checkAlter();

			switch(action) {
			case None:
				break;
			case Create:
				createTable(name());
				break;
			case Alter:
				alterTable();
				break;
			case Recreate:
				recreateTable();
				break;
			default:
				throw new UnsupportedOperationException();
			}
		} catch(SQLException e) {
			logger.error(e, Resources.format("Generator.createTableError", displayName(), "[" + connection.schema() + "]." + name(), ErrorUtils.getMessage(e)));
		} finally {
			this.table = null;

			dbFields = null;
			dbFieldsAlter = null;
			dbTable = null;
		}
	}

	public void createForeignKeys() {
		try {
			packFK();
			packIndex();
			packUnique();
		} finally {
			this.table = null;
		}
	}

	public void createRecords() {
		createNullRecord();

		for(Map<IField, primary> record : table().getStaticRecords())
			createStaticRecord(record);

		this.table = null;
	}

	GeneratorAction checkAlter() {
		GeneratorAction result = GeneratorAction.None;

		dbFields.clear();

		for(Column c : dbTable.getColumns())
			dbFields.put(c.name, new ColumnDescGen(c));

		dbFieldsAlter = new LinkedList<ColumnDescAlter>();

		for(Field field : table().getPrimaryFields()) {
			ColumnDescGen columndesc = dbFields.get(field.name());

			if(columndesc == null) {
				dbFieldsAlter.add(new ColumnDescAlter(field, FieldAction.Create, true));
				// result = GeneratorAction.Alter;
				// continue;
				result = GeneratorAction.Recreate;
				break;
			}

			columndesc.DescExist = true;

			String sqlType = field.sqlType(connection.vendor());

			if(!sqlType.startsWith(columndesc.type)) {
				result = GeneratorAction.Recreate;
				break;
			}

			if(!checkDefaults(columndesc.defaultValue, field)) {
				result = GeneratorAction.Recreate;
				break;
			}
		}

		if(result != GeneratorAction.Recreate) {
			for(ColumnDescGen field : dbFields.values()) {
				if(!field.DescExist) {
					result = GeneratorAction.Recreate;
					break;
				}
			}
		}

		return result;
	}

	static Map<String, primary> defaults = new HashMap<String, primary>();

	boolean checkDefaults(String dbDefault, Field field) {
		FieldType type = field.type();
		String key = dbDefault + '/' + type;

		primary currentDefault = defaults.get(key);
		if(currentDefault != null)
			return currentDefault.equals(field.getDefault());

		Expression expression = null;


		if(type == FieldType.Boolean)
			expression = new BoolExpression.CLASS<BoolExpression>(null).get();
		else if(type == FieldType.Guid)
			expression = new GuidExpression.CLASS<GuidExpression>(null).get();
		else if(type == FieldType.Date)
			expression = new DateExpression.CLASS<DateExpression>(null).get();
		else if(type == FieldType.Datetime)
			expression = new DatetimeExpression.CLASS<DatetimeExpression>(null).get();
		else if(type == FieldType.Datespan)
			expression = new DatespanExpression.CLASS<DatespanExpression>(null).get();
		else if(type == FieldType.Integer)
			expression = new IntegerExpression.CLASS<IntegerExpression>(null).get();
		else if(type == FieldType.Decimal)
			expression = new DecimalExpression.CLASS<DecimalExpression>(null).get();
		else if(type == FieldType.String)
			expression = new StringExpression.CLASS<StringExpression>(null).get();
		else if(type == FieldType.Text)
			expression = new TextExpression.CLASS<TextExpression>(null).get();
		else
			return true;

		expression.setExpression(new SqlStringToken(dbDefault.isEmpty() ? "null" : dbDefault, type));
		expression.setOwner(table);

		Select select = new Select();

		Query query = table();
		select.setFields(Arrays.asList((Field)expression));
		select.setRootQuery(query);

		select.open();

		if(!select.next())
			return false;

		currentDefault = expression.get();
		defaults.put(key, currentDefault);

		primary fieldDefault = field.getDefault();

		return fieldDefault.equals(currentDefault);
	}

	static void renameTable(Connection connection, String oldTableName, String newTableName) throws SQLException {
		String sql = null;

		Database database = connection.database();
		DatabaseVendor vendor = connection.vendor();
		switch(vendor) {
		case Oracle:
			sql = "rename " + database.tableName(oldTableName) + " to " + vendor.quote(newTableName);
			break;
		case SqlServer:
			sql = "sp_rename " + database.tableName(oldTableName) + ", " + vendor.quote(newTableName);
			break;
		case Postgres:
			sql = "alter table " + database.tableName(oldTableName) + " rename to " + vendor.quote(newTableName);
			break;
		default:
			throw new UnsupportedOperationException();
		}

		Statement.executeUpdate(connection, sql);
	}

	public void dropAllKeys(Connection connection) {
		this.connection = connection;
		dropFKs(connection, dbTable.getRelations(), logger);
		dropIdxs(connection, dbTable, logger);
	}

	private String getFieldForCreate(Field field) {
		DatabaseVendor vendor = vendor();
		String result = vendor.quote(field.name()) + ' ' + field.sqlType(vendor);

		if(vendor == DatabaseVendor.SqlServer || vendor == DatabaseVendor.Oracle || vendor == DatabaseVendor.Postgres) {
			result += " default " + DefaultValue.get(vendor, field);

			if(field.isPrimaryKey() || field instanceof GuidField) {
				result += " not null";
			}
		}

		return result;
	}

	private String getFieldForAlter(Field field) {
		DatabaseVendor vendor = connection.vendor();
		return vendor.quote(field.name()) + " " + field.sqlType(vendor);
	}

	private void alterTable() throws SQLException {
		boolean bAdd = false, bModify = false;

		Database database = database();
		DatabaseVendor vendor = vendor();

		String addColumnSql = "alter table " + database.tableName(table().name());
		String modifyColumnSql = "alter table " + database.tableName(table().name());

		switch(vendor) {
		case Postgres:
		case Oracle:
			addColumnSql += " add(";
			modifyColumnSql += " modify(";
			break;
		case SqlServer:
			addColumnSql += " add ";
			modifyColumnSql += " alter column ";
			break;
		}

		for(ColumnDescAlter fld : dbFieldsAlter) {
			if(fld.action == FieldAction.Create) {
				if(bAdd)
					addColumnSql += ", ";
				addColumnSql += getFieldForCreate(fld.field);
				bAdd = true;
			} else if(fld.action == FieldAction.Default) {
				if(vendor == DatabaseVendor.Oracle || vendor == DatabaseVendor.Postgres) {
					if(bModify)
						modifyColumnSql += ", ";

					String defaultValue = DefaultValue.get(connection.vendor(), fld.field);
					modifyColumnSql += vendor.quote(fld.field.name()) + " default " + defaultValue;
					bModify = true;
				} else if(vendor == DatabaseVendor.SqlServer) {
					changeMsSQLDefault(fld);
				}
			} else if(fld.action == FieldAction.AlterDefault) {
				if(vendor == DatabaseVendor.Oracle || vendor == DatabaseVendor.Postgres) {
					if(bModify)
						modifyColumnSql += ", ";
					modifyColumnSql += getFieldForCreate(fld.field);
					bModify = true;
				} else if(vendor == DatabaseVendor.SqlServer) {
					changeMsSQLDefault(fld);
				}
			} else if(fld.action == FieldAction.Alter) {
				if(bModify) {
					if(vendor == DatabaseVendor.Oracle || vendor == DatabaseVendor.Postgres) {
						modifyColumnSql += ", ";
					} else if(vendor == DatabaseVendor.SqlServer) {
						modifyColumnSql += " alter table " + database.tableName(table().name()) + " alter column ";
					}
				}
				modifyColumnSql += getFieldForAlter(fld.field);
				bModify = true;
			}
		}

		switch(vendor) {
		case Postgres:
		case Oracle:
			addColumnSql += ")";
			modifyColumnSql += ")";
			break;
		case SqlServer:
			break;
		}

		if(bAdd) {
			Statement.executeUpdate(connection, addColumnSql);
		}

		if(bModify) {
			Statement.executeUpdate(connection, modifyColumnSql);
		}
	}

	private void changeMsSQLDefault(ColumnDescAlter fld) throws SQLException {
		String defaultValue = getDefault(table().name(), fld.field.name());

		if((defaultValue != null) && (defaultValue.length() > 0)) {
			String sql = "alter table " + database().tableName(table().name()) + " drop constraint " + vendor().quote(defaultValue);
			Statement.executeUpdate(connection, sql);
		}

		defaultValue = DefaultValue.get(vendor(), fld.field);

		String sql = "alter table " + database().tableName(table().name()) + " add constraint " + vendor().quote("DF_" + table().name() + fld.field.name()) + " default " + defaultValue + " for " + vendor().quote(fld.field.name());
		Statement.executeUpdate(connection, sql);
	}

	private void recreateTable() throws SQLException {
		String tableName = table().name();

		// Никогда не пересоздаем SystemFiles - очень долго. Если что, то все изменения руками.
		if(tableName.equals(Files.TableName)) {
			logger.message(Files.TableName + " - skipped.");
			return;
		}

		String name = tableName + "_" + guid.create().toString();

		try {
			connection.beginTransaction();
			createTable(name);
			moveData(name);
			dropTable(connection, table().name());
			renameTable(connection, name, table().name());
			connection.commit();
		} catch(Throwable e) {
			connection.rollback();
			throw new SQLException(e);
		}

		updateDefaultDate();
	}

	private void createTable(String name) throws SQLException {
		String sql = "create table " + database().tableName(name) + " (";

		boolean first =  true;
		Collection<Field> fields = table().getPrimaryFields();

		for(Field field : fields) {
			sql += (first ? "" : ", ") + getFieldForCreate(field);
			first = false;
		}

		sql += ")";

		Statement.executeUpdate(connection, sql);
	}

	private void updateDefaultDate() throws SQLException {
		for(Field field : table.getDataFields()) {
			if(field instanceof DateField || field instanceof DatetimeField) {
				field.set(date.MIN);
				field.aggregation = Aggregation.None;
				date min = new date(1900, 1, 1, 0, 0, 0);
				sql_bool where = new sql_bool(new Rel(field, Operation.LT, min.sql_date()));
				new Update(table, Arrays.asList(field), null, where).execute();
			}
		}
	}

	public void createPrimaryKey() {
		try {
			new PrimaryKeyGenerator(table()).run(connection);
		} catch(ObjectAlreadyExistException e) {
		} catch(SQLException e) {
			logger.error(e, Resources.format("Generator.createUniqueIndexError", displayName(), "[" + connection.schema() + "]." + name(), ErrorUtils.getMessage(e)));
		} finally {
			this.table = null;
		}
	}

	private void packFK() {
		int c = 0;
		if(table().getForeignKeys() != null) {
			for(IForeignKey fkGen : table().getForeignKeys()) {
				while(true) {
					try {
						createFK(fkGen, c);
						break;
					} catch(ObjectAlreadyExistException e) {
						c++;
					}
				}
				c++;
			}
		}
	}

	private void createFK(IForeignKey fk, int c) {
		try {
			new ForeignKeyGenerator(new ForeignKey(name(), fk, c)).run(connection);
		} catch(SQLException e) {
			logger.error(e, Resources.format("Generator.createForeignKeyError", table().displayName(), table().name(), fk.getReferencedTable().displayName(), fk.getReferencedTable().name(), ErrorUtils.getMessage(e)));
		}
	}

	private void packIndex() {
		int index = 0;
		for(IField field : table().getIndices()) {
			try {
				index++;
				new IndexGenerator(table(), (Field)field, index, false).run(connection);
			} catch(SQLException e) {
				logger.error(e, Resources.format("Generator.createIndexError", field.displayName(), table().displayName(), table().name(), ErrorUtils.getMessage(e)));
			}
		}
	}

	private void packUnique() {
		int index = 0;
		for(IField field : table().getUniqueIndices()) {
			try {
				index++;
				new IndexGenerator(table(), (Field)field, index, true).run(connection);
			} catch(SQLException e) {
				logger.error(e, Resources.format("Generator.createUniqueIndexError", field.displayName(), table().displayName(), table().name()));
			}
		}
	}

	private void createNullRecord() {
		Query query = table();

		try {
			if(!findRecord(guid.NULL))
				query.create(guid.NULL);
		} catch(Throwable e) {
			logger.error(e, Resources.format("Generator.insertRecordsError", guid.NULL.toString(), table().displayName(), table().name(), ErrorUtils.getMessage(e)));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createStaticRecord(Map<IField, primary> record) {
		Field primaryKey = table().primaryKey();
		Field parentKey = table().parentKey();

		guid recordId = (guid)record.get(primaryKey);
		guid parentId = (guid)record.get(parentKey);

		if(parentKey != null && parentId == null) {
			parentId = guid.NULL;
		}

		Collection<Field> fields = (Collection)record.keySet();

		Query table = table();

		try {
			if(!findRecord(recordId)) {
				for(Field field : fields) {
					primary value = record.get(field);
					field.set(value);
				}
				table.create(recordId, parentId);
			} else {
				for(Field field : fields) {
					if(field.gendb_updatable()) {
						primary value = record.get(field);
						field.set(value);
					}
				}

				if(!(table instanceof Users) || !BuiltinUsers.System.guid().equals(recordId) && !BuiltinUsers.Administrator.guid().equals(recordId))
					table.update(recordId);
			}
		} catch(SQLException e) {
			logger.error(e, Resources.format("Generator.insertRecordsError", recordId.toString(), table().displayName(), table().name(), ErrorUtils.getMessage(e)));
		}

	}

	private boolean findRecord(guid recordId) throws SQLException {
		CountingSelect select = new CountingSelect();

		Query query = table();
		Field primaryKey = query.primaryKey();
		SqlToken where = new Equ(primaryKey, recordId);

		select.setWhere(where);
		select.setRootQuery(query);

		return select.count() != 0;
	}

	private void moveData(String dstTableName) throws SQLException {
		String targetFields = "";
		String sourceFields = "";

		Database database = database();
		DatabaseVendor vendor = vendor();

		for(Field field : table().getPrimaryFields()) {
			String name = field.name();

			ColumnDescGen dbField = dbFields.get(name);

			if(dbField != null) {
				targetFields += (targetFields.isEmpty() ? "" : ", ") + vendor.quote(name);

				if(field.type() == FieldType.Guid) {
					SqlToken isNull = new IsNull(new SqlField(field));
					SqlToken iif = new If(isNull, guid.NULL.sql_guid(), new SqlField(field));
					name = iif.format(vendor, new FormatOptions());
				} else if(dbField.type.startsWith("character") && field.type() == FieldType.Text) {
					name = new ToBytes(field).format(vendor, new FormatOptions());
				} else if(dbField.type.startsWith("bytea") && field.type() == FieldType.String) {
					name = new ToString(field).format(vendor, new FormatOptions());
				} else {
					name = vendor.quote(name);
				}
				sourceFields += (sourceFields.isEmpty() ? "" : ", ") + name;
			}
		}

		if(!targetFields.isEmpty()) {
			String sql = "insert into " + database.tableName(dstTableName) + " (" + targetFields + ")";
			sql += " select " + sourceFields + " from " + database.tableName(table().name()) + (vendor == DatabaseVendor.SqlServer ? " as " : "") + table().getAlias();

			Statement.executeUpdate(connection, sql);
		}
	}

	static void dropFKs(Connection connection, Collection<ForeignKey> fks, ILogger logger) {
		for(ForeignKey fk : fks) {
			try {
				fk.drop(connection);
			} catch(ObjectNotFoundException e) {
			} catch(SQLException e) {
				logger.error(e, Resources.format("Generator.dropForeignKeyError", fk.table, fk.name, ErrorUtils.getMessage(e)));
			}
		}
	}

	static void dropIdxs(Connection connection, TableDescription dbTable, ILogger logger) {
		for(Index idx : dbTable.getIndexes()) {
			try {
				IndexGenerator.dropIndex(connection, idx.tableName, idx.name);
			} catch(ObjectNotFoundException e) {
			} catch(SQLException e) {
				logger.error(e, Resources.format("Generator.dropIndexError", idx.tableName, idx.name, ErrorUtils.getMessage(e)));
			}
		}

		for(Index idx : dbTable.getUniqueIndexes()) {
			try {
				IndexGenerator.dropIndex(connection, idx.tableName, idx.name);
			} catch(ObjectNotFoundException e) {
			} catch(SQLException e) {
				logger.error(e, Resources.format("Generator.dropIndexError", idx.tableName, idx.name, ErrorUtils.getMessage(e)));
			}
		}
	}

	static void dropTable(Connection connection, String tableName) throws SQLException {
		Database database = connection.database();

		String sql = "drop table " + database.tableName(tableName);
		Statement.executeUpdate(connection, sql);
	}

	public String getDefault(String tableName, String fieldName) throws SQLException {
		String sql = "select so.name from sysobjects so, sysobjects so2, syscolumns sc where sc.name = '" + fieldName + "' and sc.id = so2.id and so2.name = " + database().tableName(tableName) + " and so.id = sc.cdefault and so.xtype = 'D'";

		Cursor cursor = BasicSelect.cursor(connection, sql);

		String result = cursor.next() ? cursor.getString(1).get() : "";
		cursor.close();
		return result;
	}
}
