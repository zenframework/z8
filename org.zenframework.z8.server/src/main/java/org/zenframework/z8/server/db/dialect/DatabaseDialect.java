package org.zenframework.z8.server.db.dialect;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.Cursor;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.SelectStatement;
import org.zenframework.z8.server.db.generator.ForeignKey;
import org.zenframework.z8.server.db.generator.Index;
import org.zenframework.z8.server.db.generator.PrimaryKey;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.SqlToken;
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

	public String normalizeIdentifier(String name) {
		return name;
	}

	public String getIndexValuesQuery(IDatabase database, String tableName, Collection<String> values) {
		throw new UnsupportedOperationException();
	}

	public List<String> parseIndexValues(String value) {
		throw new UnsupportedOperationException();
	}

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

	public Index createModelIndex(String tableName, IField field, SqlToken token, int number) {
		Index index = new Index(tableName, field, token, number);
		index.getProperties().put(Index.Method, getIndexMethod(index));
		return index;
	}

	public Index readIndex(Index.Metadata metadata) {
		Index index = new Index(metadata.tableName, metadata.name, metadata.unique, new SqlStringToken(metadata.value, FieldType.None))
				.setDefinition(metadata.definition);
		index.getProperties().put(Index.Method, metadata.method);
		index.getProperties().put(Index.Trigram, metadata.trigram);
		return index;
	}

	public String getCreateIndex(IDatabase database, Index index) {
		return "create " + (index.isUnique() ? "unique " : "") + "index " + quote(index.getName()) + " on " + formatTableName(database, index.getTableName()) + " " + formatIndexValue(index);
	}

	public String getDropIndex(IDatabase database, String tableName, String indexName) throws SQLException {
		return "drop index " + quote(database.schema()) + "." + quote(indexName);
	}

	public String getCreatePrimaryKey(IDatabase database, PrimaryKey primaryKey) {
		return "ALTER TABLE " + formatTableName(database, primaryKey.getTableName()) + " ADD PRIMARY KEY(" + StringUtils.join(quote(primaryKey.getFields()), ", ") + ")";
	}

	public String getCreateForeignKey(IDatabase database, ForeignKey foreignKey) {
		return "ALTER TABLE " + formatTableName(database, foreignKey.getTable()) + " ADD CONSTRAINT " + quote(foreignKey.getName()) + " " + "FOREIGN KEY" + "(" + quote(foreignKey.getField()) + ")" + " " + "REFERENCES " + formatTableName(database, foreignKey.getReferenceTable()) + " "
				+ "(" + quote(foreignKey.getReferenceField()) + ")";
	}

	public String getDropForeignKey(IDatabase database, ForeignKey foreignKey) {
		return "alter table " + formatTableName(database, foreignKey.getTable()) + " drop constraint " + quote(foreignKey.getOldName());
	}

	public String getUpdate(IDatabase database, String tableName, String alias) {
		return "update " + formatTableAlias(database, tableName, alias);
	}

	public String getOptimizeTable(IDatabase database, String tableName) {
		return null;
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
		case Bitset:
		case Vector:
			return result + "(" + params[0] + ")"; // length

		default:
			return result;
		}
	}

	public String formatSqlPlaceholder(FieldType type, Object... params) {
		return "?";
	}

	public String formatIndexValue(Index index) {
		throw new UnsupportedOperationException();
	}

	public String getIndexMethod(Index index) {
		throw new UnsupportedOperationException();
	}

	public String formatIndexExpression(Index index) {
		SqlToken token = index.getToken();
		FormatOptions options = new FormatOptions();
		options.disableAggregation();
		for (IField field : token.getUsedFields())
			options.setFieldAlias(field, quote(field.name()));

		return token.format(vendor(), options);
	}

	protected static boolean isView(String table) throws SQLException {
		String sql = "select type from sys.objects where type_desc = 'VIEW' and lower(name) = lower('" + table + "')";
		Cursor cursor = SelectStatement.cursor(sql);

		boolean isView = cursor.next();
		cursor.close();

		return isView;
	}
}
