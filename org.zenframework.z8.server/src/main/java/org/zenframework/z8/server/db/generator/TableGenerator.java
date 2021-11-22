package org.zenframework.z8.server.db.generator;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.table.system.Roles;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.base.table.value.BoolExpression;
import org.zenframework.z8.server.base.table.value.DatespanExpression;
import org.zenframework.z8.server.base.table.value.DatetimeExpression;
import org.zenframework.z8.server.base.table.value.DecimalExpression;
import org.zenframework.z8.server.base.table.value.Expression;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidExpression;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.base.table.value.IntegerExpression;
import org.zenframework.z8.server.base.table.value.StringExpression;
import org.zenframework.z8.server.base.table.value.TextExpression;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.CountingSelect;
import org.zenframework.z8.server.db.Cursor;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.DmlStatement;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.Select;
import org.zenframework.z8.server.db.SelectStatement;
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
import org.zenframework.z8.server.security.Role;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_integer;
import org.zenframework.z8.server.utils.ErrorUtils;

public class TableGenerator {
	private Table.CLASS<? extends Table> tableClass = null;
	private Table table = null;

	private GeneratorAction action;
	private ILogger logger;
	private Map<String, ColumnDescGen> dbFields = new HashMap<String, ColumnDescGen>();
	private Collection<ColumnDescAlter> dbFieldsAlter;
	private TableDescription dbTable;

	public TableGenerator(Table.CLASS<? extends Table> tableClass, GeneratorAction action, TableDescription dbTable, ILogger logger) {
		this.action = action;
		this.dbTable = dbTable;

		this.logger = logger;

		this.tableClass = tableClass;
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

	private Connection getConnection() {
		return ConnectionManager.get();
	}

	private Database getDatabase() {
		return getConnection().getDatabase();
	}

	private DatabaseVendor getVendor() {
		return getConnection().getVendor();
	}

	public void create() {
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
			logger.error(e, Resources.format("Generator.createTableError", displayName(), "[" + getConnection().getSchema() + "]." + name(), ErrorUtils.getMessage(e)));
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
			table = null;
		}
	}

	public void createRecords() {
		createNullRecord();

		for(Map<IField, primary> record : table().getStaticRecords())
			createStaticRecord(record);
	}

	private GeneratorAction checkAlter() {
		GeneratorAction result = GeneratorAction.None;

		dbFields.clear();

		for(Column c : dbTable.getColumns())
			dbFields.put(c.name, new ColumnDescGen(c));

		dbFieldsAlter = new LinkedList<ColumnDescAlter>();

		DatabaseVendor vendor = getVendor();

		for(Field field : table().getPrimaryFields()) {
			ColumnDescGen column = dbFields.get(vendor.sqlName(field.name()));

			if(column == null) {
				dbFieldsAlter.add(new ColumnDescAlter(field, FieldAction.Create, true));
				result = GeneratorAction.Recreate;
				break;
			}

			column.DescExist = true;

			String sqlType = field.sqlType(vendor);

			if(!sqlType.startsWith(column.type)) {
				result = GeneratorAction.Recreate;
				break;
			}

			if(!checkLength(column.size, column.scale, field) || !checkDefaults(column.defaultValue, field)) {
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

	boolean checkLength(int size, int scale, Field field) {
		FieldType type = field.type();

		if(type == FieldType.String)
			return field.size() == size;
		else if(type == FieldType.Decimal)
			return field.size() == size && field.scale() == scale;
		return true;
	}

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
		else if(type == FieldType.Date || type == FieldType.Datetime)
			expression = new DatetimeExpression.CLASS<DatetimeExpression>(null).get();
		else if(type == FieldType.Datespan)
			expression = new DatespanExpression.CLASS<DatespanExpression>(null).get();
		else if(type == FieldType.Integer)
			expression = new IntegerExpression.CLASS<IntegerExpression>(null).get();
		else if(type == FieldType.Decimal)
			expression = new DecimalExpression.CLASS<DecimalExpression>(null).get();
		else if(type == FieldType.String || type == FieldType.Geometry)
			expression = new StringExpression.CLASS<StringExpression>(null).get();
		else if(type == FieldType.Text || type == FieldType.Attachments || type == FieldType.File)
			expression = new TextExpression.CLASS<TextExpression>(null).get();
		else
			return true;

		expression.setExpression(new SqlStringToken(dbDefault.isEmpty() ? "null" : dbDefault, type));
		expression.setOwner(table);

		Select select = new Select();
		select.setFields(Arrays.asList((Field)expression));
		select.open();

		if(!select.next())
			return false;

		currentDefault = expression.get();
		defaults.put(key, currentDefault);

		primary fieldDefault = field.getDefault();

		return fieldDefault.equals(currentDefault);
	}

	static void renameTable(String oldTableName, String newTableName) throws SQLException {
		String sql = null;

		Connection connection = ConnectionManager.get();
		Database database = connection.getDatabase();
		DatabaseVendor vendor = connection.getVendor();

		switch(vendor) {
		case Oracle:
			sql = "alter table " + database.getTableName(oldTableName) + " rename  to " + vendor.quote(newTableName);
			break;
		case SqlServer:
			sql = "sp_rename " + database.getTableName(oldTableName) + ", " + vendor.quote(newTableName);
			break;
		case Postgres:
		case H2:
			sql = "alter table " + database.getTableName(oldTableName) + " rename to " + vendor.quote(newTableName);
			break;
		default:
			throw new UnsupportedOperationException();
		}

		DmlStatement.execute(sql);
	}

	public void dropAllKeys() {
		dropFKs(dbTable.getRelations(), logger);
		dropIdxs(dbTable, logger);
	}

	private String formatDefaultValue(DatabaseVendor vendor, Field field) {
		primary value = field.getDefaultValue();
		FieldType type = field.type();

		if(type == FieldType.Text || type == FieldType.Attachments || type == FieldType.File)
			value = new binary((string)value);

		return value.toDbConstant(vendor);
	}

	private String getFieldForCreate(Field field) {
		DatabaseVendor vendor = getVendor();
		String result = vendor.quote(field.name()) + ' ' + field.sqlType(vendor);

		result += " default " + formatDefaultValue(vendor, field);

		if(field.isPrimaryKey() || field instanceof GuidField) {
			result += " not null";
		}

		return result;
	}

	private String getFieldForAlter(Field field) {
		DatabaseVendor vendor = getConnection().getVendor();
		return vendor.quote(field.name()) + " " + field.sqlType(vendor);
	}

	private void alterTable() throws SQLException {
		boolean bAdd = false, bModify = false;

		Database database = getDatabase();
		DatabaseVendor vendor = getVendor();

		String addColumnSql = "alter table " + database.getTableName(table().name());
		String modifyColumnSql = "alter table " + database.getTableName(table().name());

		switch(vendor) {
		case Postgres:
		case Oracle:
		case H2:
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

					String defaultValue = formatDefaultValue(vendor, fld.field);
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
						modifyColumnSql += " alter table " + database.getTableName(table().name()) + " alter column ";
					}
				}
				modifyColumnSql += getFieldForAlter(fld.field);
				bModify = true;
			}
		}

		switch(vendor) {
		case Postgres:
		case Oracle:
		case H2:
			addColumnSql += ")";
			modifyColumnSql += ")";
			break;
		case SqlServer:
			break;
		}

		if(bAdd) {
			DmlStatement.execute(addColumnSql);
		}

		if(bModify) {
			DmlStatement.execute(modifyColumnSql);
		}
	}

	private void changeMsSQLDefault(ColumnDescAlter fld) throws SQLException {
		String defaultValue = getDefault(table().name(), fld.field.name());

		if((defaultValue != null) && (defaultValue.length() > 0)) {
			String sql = "alter table " + getDatabase().getTableName(table().name()) + " drop constraint " + getVendor().quote(defaultValue);
			DmlStatement.execute(sql);
		}

		defaultValue = formatDefaultValue(getVendor(), fld.field);

		String sql = "alter table " + getDatabase().getTableName(table().name()) + " add constraint " + getVendor().quote("DF_" + table().name() + fld.field.name()) + " default " + defaultValue + " for " + getVendor().quote(fld.field.name());
		DmlStatement.execute(sql);
	}

	private void recreateTable() throws SQLException {
		String tableName = table().name();

		// Никогда не пересоздаем SystemFiles - очень долго. Если что - все изменения руками.
		if(tableName.equals(Files.TableName)) {
//			logger.info(Files.TableName + " - skipped.");
//			return;
		}

		String name = "" + Math.abs(tableName.hashCode());

		Connection connection = getConnection();

		try {
			connection.beginTransaction();
			createTable(name);
			if (connection.getVendor() == DatabaseVendor.H2)
				connection.flush();
			moveData(name);
			dropTable(table().name());
			renameTable(name, table().name());
			connection.commit();
		} catch(Throwable e) {
			connection.rollback();
			throw new SQLException(e);
		}
	}

	private void createTable(String name) throws SQLException {
		String sql = "create table " + getDatabase().getTableName(name) + " (";

		boolean first =  true;
		Collection<Field> fields = table().getPrimaryFields();

		for(Field field : fields) {
			sql += (first ? "" : ", ") + getFieldForCreate(field);
			first = false;
		}

		sql += ")";

		DmlStatement.execute(sql);
	}

	public void createPrimaryKey() {
		try {
			new PrimaryKeyGenerator(table()).run();
		} catch(ObjectAlreadyExistException e) {
		} catch(SQLException e) {
			logger.error(e, Resources.format("Generator.createUniqueIndexError", displayName(), "[" + getConnection().getSchema() + "]." + name(), ErrorUtils.getMessage(e)));
		} finally {
			this.table = null;
		}
	}

	private void packFK() {
		int index = 0;
		if(table().getForeignKeys() != null) {
			for(IForeignKey foreignKey : table().getForeignKeys()) {
				while(true) {
					try {
						createForeignKey(foreignKey, index);
						break;
					} catch(ObjectAlreadyExistException e) {
						index++;
					}
				}
				index++;
			}
		}
	}

	private void createForeignKey(IForeignKey foreignKey, int index) {
		try {
			new ForeignKeyGenerator(new ForeignKey(name(), foreignKey, index)).run();
		} catch(SQLException e) {
			logger.error(e, Resources.format("Generator.createForeignKeyError", table().displayName(), table().name(), foreignKey.getReferencedTable().displayName(), foreignKey.getReferencedTable().name(), ErrorUtils.getMessage(e)));
		}
	}

	private void packIndex() {
		int index = 0;
		for(IField field : table().getIndices()) {
			try {
				index++;
				new IndexGenerator(table(), (Field)field, index).run();
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
				new IndexGenerator(table(), (Field)field, index).run();
			} catch(SQLException e) {
				logger.error(e, Resources.format("Generator.createUniqueIndexError", field.displayName(), table().displayName(), table().name()));
			}
		}
	}

	private void createNullRecord() {
		Query query = table();

		try {
			if(!findRecord(guid.Null))
				query.create(guid.Null);
		} catch(Throwable e) {
			logger.error(e, Resources.format("Generator.insertRecordsError", guid.Null.toString(), table().displayName(), table().name(), ErrorUtils.getMessage(e)));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createStaticRecord(Map<IField, primary> record) {
		Field primaryKey = table().primaryKey();

		guid recordId = (guid)record.get(primaryKey);

		Collection<Field> fields = (Collection)record.keySet();

		Query table = table();

		try {
			if(!findRecord(recordId)) {
				for(Field field : fields) {
					primary value = record.get(field);
					field.set(value);
				}
				table.create(recordId);
			} else {
				boolean isUsers = table instanceof Users && (BuiltinUsers.System.guid().equals(recordId) || 
						BuiltinUsers.Administrator.guid().equals(recordId));

				boolean isRoles = table instanceof Roles && (Role.Administrator.equals(recordId) || 
						Role.User.equals(recordId) || Role.Guest.equals(recordId));

				Users users = isUsers ? (Users)table : null;
				Roles roles = isRoles ? (Roles)table : null;

				for(Field field : fields) {
					if(isUsers && (field == users.password.get() || field == users.name.get() || field == users.description.get()))
						continue;

					if(isRoles && (field == roles.name.get() || field == roles.description.get()))
						continue;

					primary value = record.get(field);
					field.set(value);
				}

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

		Database database = getDatabase();
		DatabaseVendor vendor = getVendor();
		boolean postgres = vendor == DatabaseVendor.Postgres || vendor == DatabaseVendor.H2;
		boolean oracle = vendor == DatabaseVendor.Oracle;

		FormatOptions options = new FormatOptions();
		options.disableAggregation();

		for(Field field : table().getPrimaryFields()) {
			String name = field.name();

			ColumnDescGen dbField = dbFields.get(name);

			if(dbField != null) {
				FieldType type = field.type();

				targetFields += (targetFields.isEmpty() ? "" : ", ") + vendor.quote(name);

				if(type == FieldType.Guid)
					name = new If(new IsNull(field), guid.Null.sql_guid(), new SqlField(field)).format(vendor, options);
				else if(postgres && dbField.type.equals("uuid") && type == FieldType.Integer)
					name = "null";
				else if(postgres && dbField.type.startsWith("character") && type == FieldType.Text)
					name = new ToBytes(field).format(vendor, options);
				else if(postgres && dbField.type.startsWith("bytea") && type == FieldType.String)
					name = new ToString(field).format(vendor, options);
				else if(oracle && (dbField.type.startsWith("BLOB") || dbField.type.startsWith("NCLOB")))
					name = "null";
				else if(postgres && dbField.type.startsWith("timestamp") && (type == FieldType.Date || type == FieldType.Datetime)) {
					SqlToken condition = new Rel(field, Operation.LT, new SqlStringToken("'1900-01-01 00:00:00'", FieldType.Datetime));
					SqlToken yes = new sql_integer(date.UtcMin);
					SqlToken no = new SqlStringToken("extract(epoch from " + new SqlField(field).format(vendor, options) + ") * 1000", FieldType.Integer);
					name = new If(condition, yes, no).format(vendor, options);
				} else
					name = vendor.quote(name);

				sourceFields += (sourceFields.isEmpty() ? "" : ", ") + name;
			}
		}

		options.enableAggregation();

		if(!targetFields.isEmpty()) {
			String sql = "insert into " + database.getTableName(dstTableName) + " (" + targetFields + ")";
			sql += " select " + sourceFields + " from " + database.getTableName(table().name()) + (vendor == DatabaseVendor.SqlServer ? " as " : "") + table().getAlias();

			DmlStatement.execute(sql);
		}
	}

	static void dropFKs(Collection<ForeignKey> fks, ILogger logger) {
		for(ForeignKey fk : fks) {
			try {
				fk.drop();
			} catch(ObjectNotFoundException e) {
			} catch(SQLException e) {
				logger.error(e, Resources.format("Generator.dropForeignKeyError", fk.table, fk.name, ErrorUtils.getMessage(e)));
			}
		}
	}

	static void dropIdxs(TableDescription dbTable, ILogger logger) {
		for(Index idx : dbTable.getIndexes()) {
			try {
				IndexGenerator.dropIndex(idx.tableName, idx.name);
			} catch(ObjectNotFoundException e) {
			} catch(SQLException e) {
				logger.error(e, Resources.format("Generator.dropIndexError", idx.tableName, idx.name, ErrorUtils.getMessage(e)));
			}
		}

		for(Index idx : dbTable.getUniqueIndexes()) {
			try {
				IndexGenerator.dropIndex( idx.tableName, idx.name);
			} catch(ObjectNotFoundException e) {
			} catch(SQLException e) {
				logger.error(e, Resources.format("Generator.dropIndexError", idx.tableName, idx.name, ErrorUtils.getMessage(e)));
			}
		}
	}

	static void dropTable(String tableName) throws SQLException {
		Connection connection = ConnectionManager.get();
		Database database = connection.getDatabase();

		String sql = "drop table " + database.getTableName(tableName);
		DmlStatement.execute(sql);
	}

	public String getDefault(String tableName, String fieldName) throws SQLException {
		String sql = "select so.name from sysobjects so, sysobjects so2, syscolumns sc where sc.name = '" + fieldName + "' and sc.id = so2.id and so2.name = " + getDatabase().getTableName(tableName) + " and so.id = sc.cdefault and so.xtype = 'D'";

		Cursor cursor = SelectStatement.cursor(sql);

		String result = cursor.next() ? cursor.getString(1).get() : "";
		cursor.close();
		return result;
	}
}
