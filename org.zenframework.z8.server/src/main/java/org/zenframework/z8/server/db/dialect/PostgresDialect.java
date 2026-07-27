package org.zenframework.z8.server.db.dialect;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.engine.IDatabase;

public class PostgresDialect extends DatabaseDialect {

	public static final String Name = "Postgres";

	@Override
	public String name() {
		return Name;
	}

	@Override
	public DatabaseVendor vendor() {
		return DatabaseVendor.Postgres;
	}

	@Override
	public String getTables(IDatabase database) {
		return "SELECT " + 
				"table_name, " +
				"column_name, " +
				"data_type, " +
				"coalesce(character_maximum_length, numeric_precision, 0), " +
				"coalesce(numeric_scale, 0), " + "case when is_nullable = 'NO' then 0 else 1 end, " +
				"coalesce(column_default, '') " +
			"FROM information_schema.columns " + 
			"WHERE table_schema = '" + database.schema() + "' " +
			"ORDER BY table_name, ordinal_position";
	}

	@Override
	public String getPrimaryKeys(IDatabase database) {
		return "select " + "keys.constraint_name, " + "keys.table_name, " + "cols.column_name " + "from " + "information_schema.table_constraints keys, " + "information_schema.key_column_usage cols " + "where " + "keys.constraint_name = cols.constraint_name and "
				+ "keys.constraint_type = 'PRIMARY KEY' and " + "keys.constraint_schema = '" + database.schema() + "'" + "order by " + "keys.table_name, cols.ordinal_position";
	}

	@Override
	public String getForeignKeys(IDatabase database) {
		return "select " + "pk.table_name, " + "pk.column_name, " + "fk.table_name, " + "fk.column_name, " + "fk.constraint_name " + "from " + "information_schema.key_column_usage fk, " + "information_schema.referential_constraints refc, "
				+ "information_schema.key_column_usage pk " + "where " + "fk.constraint_name = refc.constraint_name and " + "refc.unique_constraint_name = pk.constraint_name and " + "fk.constraint_schema = '" + database.schema() + "'";
	}

	@Override
	public String getIndixes(IDatabase database) {
		return "select indexes.relname as index, max(tables.relname) as table, max(columns.attname) as column, bool_or(root.indisunique) as uniqueindex"
				+ " from pg_class tables, pg_class indexes, pg_namespace owners, pg_index root, pg_attribute columns"
				+ " where tables.oid = root.indrelid and indexes.oid = root.indexrelid and owners.oid = tables.relnamespace and tables.oid = columns.attrelid"
				+ " and columns.attnum > 0 and (columns.attnum = ANY(root.indkey) or root.indkey = '0' and root.indexprs is not null) and  tables.relkind = 'r'"
				// + " and root.indisunique = '" + (unique ? "t" : "f") + "'"
				+ " and not root.indisprimary and owners.nspname = '" + database.schema() + "'"
				+ " group by index";
	}

	@Override
	public String getOptimizeTable(IDatabase database, String tableName) {
		return "vacuum analyze " + formatTableName(database, tableName);
	}

	@Override
	public String formatDefaultValue(String defaultValue, String typeName) {
		defaultValue = defaultValue.replace("::" + typeName, "");

		if (formatType(FieldType.Date).equals(typeName))
			defaultValue = defaultValue.replace("::text", "").replace(", ", ",");

		if (formatType(FieldType.Text).equals(typeName) && defaultValue.isEmpty())
			defaultValue = "null";

		return defaultValue;
	}

	@Override
	public String formatType(FieldType type) {
		switch(type) {
		case Attachments:
		case Binary:
		case File:
		case Text:
			return "bytea";
		case Boolean:
			return "smallint";
		case Date:
		case Datetime:
		case Datespan:
		case Integer:
			return "bigint";
		case Decimal:
			return "numeric";
		case Geometry:
			return "geometry";
		case Guid:
			return "uuid";
		case String:
			return "character varying";
		default:
			throw new RuntimeException("Unknown data type: '" + type.toString() + "'");
		}
	}

	@Override
	public String formatSqlType(FieldType type, Object... params) {
		String result = formatType(type);

		switch(type) {
		case Geometry:
			return result + "(Geometry, " + params[0] + ")"; // SRS

		default:
			return result;
		}
	}
}
