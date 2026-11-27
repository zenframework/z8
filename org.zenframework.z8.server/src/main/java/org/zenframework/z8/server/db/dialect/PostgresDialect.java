package org.zenframework.z8.server.db.dialect;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.generator.Index;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlFieldList;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.fts.Fts;
import org.zenframework.z8.server.db.sql.fts.FtsVector;
import org.zenframework.z8.server.engine.IDatabase;
import org.zenframework.z8.server.json.parser.JsonArray;

public class PostgresDialect extends DatabaseDialect {

	public static final String Name = "Postgres";
	private static final int MaxIdentifierBytes = 63;

	@Override
	public String normalizeIdentifier(String name) {
		while (name.getBytes(StandardCharsets.UTF_8).length > MaxIdentifierBytes)
			name = name.substring(0, name.offsetByCodePoints(name.length(), -1));
		return name;
	}

	@Override
	public String formatIndexValue(Index index) {
		String indexValue = formatIndexExpression(index);
		if (!(index.getToken() instanceof SqlField) && !(index.getToken() instanceof SqlFieldList))
			indexValue = "(" + indexValue + ")";

		String method = getIndexMethod(index);
		String operatorClass = index.fieldType() == FieldType.Vector ? " vector_cosine_ops" : index.trigram() ? " gin_trgm_ops" : "";
		return (method.equals("btree") ? "" : "using " + method + " ") + "(" + indexValue + operatorClass + ")";
	}

	@Override
	public String getIndexMethod(Index index) {
		switch(index.fieldType()) {
		case Geometry:
			return "gist";
		case Vector:
			return "hnsw";
		case FtsVector:
			return "gin";
		default:
			return index.trigram() ? "gin" : "btree";
		}
	}

	@Override
	public String getIndexValuesQuery(IDatabase database, String tableName, Collection<String> values) {
		return "explain (verbose, costs false, format json) select ROW(" + StringUtils.join(values, "), ROW(")
				+ ") from " + formatTableName(database, tableName) + " limit 0";
	}

	@Override
	public List<String> parseIndexValues(String value) {
		JsonArray output = new JsonArray(value).getJsonObject(0).getJsonObject("Plan").getJsonArray("Output");
		List<String> values = new ArrayList<String>();
		for (int i = 0; i < output.size(); i++)
			values.add(output.getString(i));
		return values;
	}

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
				"case when data_type = 'USER-DEFINED' then udt_name else data_type end, " +
				"coalesce(character_maximum_length, numeric_precision, " +
					"(select substring(format_type(attribute.atttypid, attribute.atttypmod) from ',([0-9]+)[)]$')::integer " +
					"from pg_attribute attribute where udt_name = 'geometry' " +
					"and attribute.attrelid = (quote_ident(table_schema) || '.' || quote_ident(table_name))::regclass " +
					"and attribute.attname = column_name), 0), " +
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
		return "select "
				+ "indexes.relname as index, "
				+ "tables.relname as table, "
				+ "string_agg(case when columns.attnum is not null then quote_ident(columns.attname) "
				+ "else pg_get_indexdef(indexes.oid, keys.position, false) end, ', ' order by keys.position) as value, "
				+ "root.indisunique as uniqueindex, "
				+ "methods.amname as method, "
				+ "bool_or(classes.opcname = 'gin_trgm_ops') as trigram, "
				+ "pg_get_indexdef(indexes.oid) as definition "
				+ "from pg_index root "
				+ "join pg_class tables on tables.oid = root.indrelid "
				+ "join pg_class indexes on indexes.oid = root.indexrelid "
				+ "join pg_namespace owners on owners.oid = tables.relnamespace "
				+ "join pg_am methods on methods.oid = indexes.relam "
				+ "cross join lateral generate_series(1, root.indnkeyatts) keys(position) "
				+ "left join pg_attribute columns on columns.attrelid = root.indrelid and columns.attnum = root.indkey[keys.position - 1] and columns.attnum > 0 "
				+ "left join pg_opclass classes on classes.oid = root.indclass[keys.position - 1] "
				+ "where tables.relkind = 'r' and not root.indisprimary and owners.nspname = '" + database.schema() + "' "
				+ "group by indexes.oid, indexes.relname, tables.relname, root.indexrelid, root.indrelid, root.indisunique, methods.amname";
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
		case FtsVector:
			return "tsvector";
		case Vector:
			return "vector";
		case Bitset:
			return "bit varying";
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

	@Override
	public String formatSqlPlaceholder(FieldType type, Object... params) {
		switch(type) {
		case FtsVector:
			return new FtsVector(new SqlStringToken("?", type), (Fts) params[0]).format(vendor(), new FormatOptions());
		case Bitset:
			return "?::bit(" + params[0] + ")"; // Length
		case Vector:
			return "?::" + formatType(type);
		default:
			return super.formatSqlPlaceholder(type, params);
		}
	}

}
