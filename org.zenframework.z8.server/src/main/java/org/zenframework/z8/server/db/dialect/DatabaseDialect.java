package org.zenframework.z8.server.db.dialect;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.db.Cursor;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.SelectStatement;
import org.zenframework.z8.server.db.generator.ForeignKey;
import org.zenframework.z8.server.db.generator.Index;
import org.zenframework.z8.server.db.generator.PrimaryKey;
import org.zenframework.z8.server.engine.IDatabase;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public abstract class DatabaseDialect {

	public static final DatabaseDialect Default = new PostgresDialect();

	public abstract String name();
	public abstract DatabaseVendor vendor();

	// DataSchema

	public abstract String getTables(IDatabase database);
	public abstract String getPrimaryKeys(IDatabase database);
	public abstract String getForeignKeys(IDatabase database);
	public abstract String getIndixes(IDatabase database);

	public String getRenameTable(IDatabase database, String oldName, String newName) {
		return "alter table " + formatTableName(database, oldName) + " rename  to " + quote(newName);
	}

	public String getCreateTable(IDatabase database, String name, Collection<Field> fields) {
		String sql = "create table " + formatTableName(database, name) + " (";

		boolean first =  true;

		for (Field field : fields) {
			sql += (first ? "" : ", ") + formatCreateField(field);
			first = false;
		}

		return sql + ")";
	}

	public String getDropTable(IDatabase database, String name) {
		return "drop table " + formatTableName(database, name);
	}

	public String getCreateIndex(IDatabase database, Index index) {
		return "create " + (index.unique ? "unique " : "") + "index " + quote(index.name) + " on " + formatTableName(database, index.tableName) + " " + formatIndexField(index);
	}

	public String getDropIndex(IDatabase database, String tableName, String indexName) throws SQLException {
		return "drop index " + quote(database.schema()) + "." + quote(indexName);
	}

	public String getCreatePrimaryKey(IDatabase database, PrimaryKey primaryKey) {
		return "ALTER TABLE " + formatTableName(database, primaryKey.tableName) + " ADD PRIMARY KEY(" + StringUtils.join(quote(primaryKey.getFields()), ", ") + ")";
	}

	public String getCreateForeignKey(IDatabase database, ForeignKey foreignKey) {
		return "ALTER TABLE " + formatTableName(database, foreignKey.getTable()) + " ADD CONSTRAINT " + quote(foreignKey.getName()) + " " + "FOREIGN KEY" + "(" + quote(foreignKey.getField()) + ")" + " " + "REFERENCES " + formatTableName(database, foreignKey.getReferenceTable()) + " "
				+ "(" + quote(foreignKey.getReferenceField()) + ")";
	}

	public String getDropForeignKey(IDatabase database, ForeignKey foreignKey) {
		return "alter table " + formatTableName(database, foreignKey.getTable()) + " drop constraint " + quote(foreignKey.getName());
	}

	public String getUpdate(IDatabase database, String tableName, String alias) {
		return "update " + formatTableAlias(database, tableName, alias);
	}

	public String quote(String name) {
		return '"' + name + '"';
	}

	public Collection<String> quote(Collection<String> names) {
		Collection<String> formatted = new ArrayList<String>(names.size());

		for (String name : names)
			formatted.add(quote(name));

		return formatted;
	}

	public String formatTableName(IDatabase database, String name) {
		return quote(database.schema()) + "." + quote(name);
	}

	public String formatTableAlias(IDatabase database, String tableName, String alias) {
		return formatTableName(database, tableName) + ' ' + alias;
	}

	public String formatCreateField(Field field) {
		String result = quote(field.name()) + ' ' + field.sqlType(vendor());

		result += " default " + formatDefaultValue(field);

		if (field.isPrimaryKey() || field instanceof GuidField)
			result += " not null";

		return result;
	}

	public String formatDefaultValue(Field field) {
		primary value = field.getDefaultValue();
		FieldType type = field.type();

		if (type == FieldType.Text || type == FieldType.Attachments || type == FieldType.File)
			value = new binary((string) value);

		return value.toDbConstant(vendor());
	}

	public String formatSqlName(String name) {
		return name;
	}

	public int formatLength(int length, String typeName) {
		return length;
	}

	public String formatDefaultValue(String defaultValue, String typeName) {
		return defaultValue;
	}

	public abstract String formatType(FieldType type);

	public String formatSqlType(FieldType type, Object... params) {
		String result = formatType(type);

		switch(type) {
		case Decimal:
			return result + "(" + params[0] + ", " + params[1] + ")"; // precision, scale

		case String:
			return result + "(" + params[0] + ")"; // length

		default:
			return result;
		}
	}

	public String formatIndexField(Index index) {
		String fieldName = quote(index.fields.get(0));
		switch(index.fieldType()) {
		case String:
			boolean trigram = index.trigram();
			boolean caseInsensitive = index.caseInsensitive();
			return (trigram ? "using gin " : "") + "(" + (caseInsensitive ? "LOWER(" : "") + fieldName + (trigram ? " gin_trgm_ops" : "") + (caseInsensitive ? ")" : "") + ")";
		case Geometry:
			return "using gist (" + fieldName + ")";
		default:
			return "(" + fieldName  + ")";
		}
	}

	protected static boolean isView(String table) throws SQLException {
		String sql = "select type from sys.objects where type_desc = 'VIEW' and lower(name) = lower('" + table + "')";
		Cursor cursor = SelectStatement.cursor(sql);

		boolean isView = cursor.next();
		cursor.close();

		return isView;
	}
}
