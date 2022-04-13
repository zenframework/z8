package org.zenframework.z8.server.db.generator;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.zenframework.z8.server.db.SelectStatement;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.Cursor;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class DataSchema {
	public static Map<String, TableDescription> getTables(String tablePattern) throws SQLException {
		LinkedHashMap<String, TableDescription> tables = new LinkedHashMap<String, TableDescription>();

		switch(ConnectionManager.vendor()) {
		case Oracle:
			collectOracleColumns(tables);
			break;
		case SqlServer:
			collectSqlServerColumns(tables, tablePattern);
			break;
		case Postgres:
		case H2:
			collectPostgresColumns(tables);
			break;
		default:
			throw new UnknownDatabaseException();
		}

		for(Map.Entry<String, PrimaryKey> eidx : getPrimaryKeys(tablePattern).entrySet()) {
			if(tables.containsKey(eidx.getKey()))
				tables.get(eidx.getKey()).setPK(eidx.getValue());
		}

		for(Index idx : getIndices(false, tablePattern)) {
			if(tables.containsKey(idx.tableName))
				tables.get(idx.tableName).addIndex(idx);
		}

		for(Index idx : getIndices(true, tablePattern)) {
			if(tables.containsKey(idx.tableName))
				tables.get(idx.tableName).addUniqueIndex(idx);
		}

		for(ForeignKey relation : getForeignKeys(tablePattern)) {
			if(tables.containsKey(relation.table))
				tables.get(relation.table).addLink(relation);
		}

		return tables;
	}

	static private void collectOracleColumns(Map<String, TableDescription> tables) throws SQLException {
		String sql = 
			"SELECT " + 
				"TABLE_NAME, " +
				"COLUMN_NAME, " +
				"DATA_TYPE, " +
				"COALESCE(CHAR_COL_DECL_LENGTH, DATA_PRECISION, 0), " +
				"COALESCE(DATA_SCALE, 0), " + 
				"CASE WHEN NULLABLE = 'NO' THEN 0 ELSE 1 END, " +
				"NVL(DATA_DEFAULT, '') " +
			"FROM ALL_TAB_COLUMNS " +
			"WHERE OWNER = '" + ConnectionManager.database().schema() + "' " +
			"ORDER BY TABLE_NAME, COLUMN_ID";

		Cursor cursor = SelectStatement.cursor(sql);

		while(cursor.next()) {
			String tableName = cursor.getString(1).get();

			if(!tables.containsKey(tableName))
				tables.put(tableName, new TableDescription(tableName, false));

			String fieldName = cursor.getString(2).get();
			String typeName = cursor.getString(3).get();
			int length = cursor.getInteger(4).getInt();
			int scale = cursor.getInteger(5).getInt();
			boolean nullable = cursor.getBoolean(6).get();
			String defaultValue = cursor.getString(7).get().replace("::" + typeName, "");

			tables.get(tableName).addField(new Column(fieldName, typeName, length, scale, nullable, defaultValue));
		}

		cursor.close();
	}

	static private void collectPostgresColumns(Map<String, TableDescription> tables) throws SQLException {
		DatabaseVendor vendor = ConnectionManager.vendor();
		String sql = 
			"SELECT " + 
				"table_name, " +
				"column_name, " +
				"data_type, " +
				"coalesce(character_maximum_length, numeric_precision, 0), " +
				"coalesce(numeric_scale, 0), " + "case when is_nullable = 'NO' then 0 else 1 end, " +
				"coalesce(column_default, '') " +
			"FROM information_schema.columns " + 
			"WHERE table_schema = '" + ConnectionManager.database().schema() + "' " +
			"ORDER BY table_name, ordinal_position";

		Cursor cursor = SelectStatement.cursor(sql);

		while(cursor.next()) {
			String tableName = cursor.getString(1).get();

			if(!tables.containsKey(tableName)) {
				tables.put(tableName, new TableDescription(tableName, false));
			}

			String fieldName = cursor.getString(2).get();
			String typeName = cursor.getString(3).get();
			int length = cursor.getInteger(4).getInt();
			int scale = cursor.getInteger(5).getInt();
			boolean nullable = cursor.getBoolean(6).get();
			String defaultValue = cursor.getString(7).get().replace("::" + typeName, "");

			if(FieldType.Date.vendorType(vendor).equals(typeName))
				defaultValue = defaultValue.replace("::text", "").replace(", ", ",");

			if(FieldType.Text.vendorType(vendor).equals(typeName) && defaultValue.isEmpty())
				defaultValue = "null";

			tables.get(tableName).addField(new Column(fieldName, typeName, length, scale, nullable, defaultValue));
		}

		cursor.close();
	}

	static private void collectSqlServerColumns(Map<String, TableDescription> tables, String tablePattern) throws SQLException {
		String sql = "SELECT " + "cast(TABLE_NAME as nvarchar(max)), " + "cast(COLUMN_NAME as nvarchar(max)), " + "cast(DATA_TYPE as nvarchar(max)), " + "isnull(CHARACTER_MAXIMUM_LENGTH, isnull(NUMERIC_PRECISION, 0)) COLUMN_SIZE, " + "isnull(NUMERIC_SCALE, 0) DECIMAL_DIGITS, "
				+ "(case when IS_NULLABLE = 'NO' then 0 else 1 end) NULLABLE, " + "cast(isnull(COLUMN_DEFAULT, '') as nvarchar(max)) " + "FROM " + "INFORMATION_SCHEMA.COLUMNS " + "WHERE " + "lower(TABLE_NAME) like lower('" + tablePattern + "')" + "ORDER BY "
				+ "TABLE_NAME, ORDINAL_POSITION";

		Cursor cursor = SelectStatement.cursor(sql);

		while(cursor.next()) {
			String tableName = cursor.getString(1).get().toUpperCase();

			if(!tables.containsKey(tableName)) {
				tables.put(tableName, new TableDescription(tableName, isView(tableName)));
			}

			String field_name = cursor.getString(2).get();
			String type_name = cursor.getString(3).get();

			tables.get(tableName).addField(new Column(field_name, type_name, (type_name.equalsIgnoreCase("uniqueidentifier") ? 36 : cursor.getInteger(4).getInt()), cursor.getInteger(5).getInt(), cursor.getBoolean(6).get(), cursor.getString(7).get()));
		}

		cursor.close();
	}

	static boolean isView(String table) throws SQLException {
		String sql = "select type from sys.objects where type_desc = 'VIEW' and lower(name) = lower('" + table + "')";
		Cursor cursor = SelectStatement.cursor(sql);

		boolean isView = cursor.next();
		cursor.close();

		return isView;
	}

	static private Map<String, PrimaryKey> getPrimaryKeys(String tableLike) throws SQLException {
		String sql;

		switch(ConnectionManager.vendor()) {
		case Oracle:
			sql = "SELECT TO_NCHAR(pkCols.CONSTRAINT_NAME)," + "  TO_NCHAR(pkCols.TABLE_NAME)," + "  TO_NCHAR(pkCols.COLUMN_NAME)" + " FROM user_constraints pk," + "  user_cons_columns pkCols" + " WHERE pk.constraint_type = 'P'"
					+ "  AND pk.CONSTRAINT_NAME     =pkCols.CONSTRAINT_NAME" + "  AND lower(pkCols.TABLE_NAME) LIKE lower('" + tableLike + "')" + " ORDER BY pkCols.TABLE_NAME," + "  pkCols.POSITION";
			break;
		case SqlServer:
			sql = "select " + "CAST(pk.CONSTRAINT_NAME as nvarchar(255)), " + "CAST(pk.TABLE_NAME as nvarchar(255)), " + "CAST(pkCols.COLUMN_NAME as nvarchar(255)) " + "from " + "INFORMATION_SCHEMA.TABLE_CONSTRAINTS pk, " + "INFORMATION_SCHEMA.KEY_COLUMN_USAGE pkCols " + "where "
					+ "pk.CONSTRAINT_NAME = pkCols.CONSTRAINT_NAME and " + "pk.CONSTRAINT_TYPE = 'PRIMARY KEY' and " + "lower(pk.TABLE_NAME) like lower('" + tableLike + "')" + "order by " + "pk.TABLE_NAME, pkcols.ORDINAL_POSITION";
			break;
		case Postgres:
		case H2:
			sql = "select " + "keys.constraint_name, " + "keys.table_name, " + "cols.column_name " + "from " + "information_schema.table_constraints keys, " + "information_schema.key_column_usage cols " + "where " + "keys.constraint_name = cols.constraint_name and "
					+ "keys.constraint_type = 'PRIMARY KEY' and " + "keys.constraint_schema = '" + ConnectionManager.database().schema() + "'" + "order by " + "keys.table_name, cols.ordinal_position";
			break;
		default:
			throw new UnknownDatabaseException();
		}

		Map<String, PrimaryKey> primaryKeys = new Hashtable<String, PrimaryKey>();

		Cursor cursor = SelectStatement.cursor(sql);

		while(cursor.next()) {
			String pk_name = cursor.getString(1).get();
			String table_name = cursor.getString(2).get();
			String col_name = cursor.getString(3).get();

			if(primaryKeys.containsKey(table_name)) {
				primaryKeys.get(table_name).fields.add(col_name);
			} else {
				primaryKeys.put(table_name, new PrimaryKey(pk_name, table_name, col_name));
			}
		}

		cursor.close();

		return primaryKeys;
	}

	static private Iterable<Index> getIndices(boolean unique, String tableLike) throws SQLException {
		String sql;

		switch(ConnectionManager.vendor()) {
		case Oracle:
			sql = "SELECT TO_NCHAR(a.INDEX_NAME) INDEX_NAME, TO_NCHAR(a.TABLE_NAME) TABLE_NAME, TO_NCHAR(a.COLUMN_NAME) COLUMN_NAME" + "  FROM user_ind_columns a ," + " (SELECT a.INDEX_NAME" + "    FROM user_indexes a" + "   WHERE UNIQUENESS = '"
					+ (unique ? "UNIQUE" : "NONUNIQUE") + "'" + " AND lower(a.table_name) LIKE lower('" + tableLike + "')" + " ) b" + " WHERE a.INDEX_NAME = b.INDEX_NAME" + " AND NOT EXISTS" + "  (SELECT 0" + "     FROM user_constraints pk" + "    WHERE pk.constraint_type = 'P'"
					+ "  AND pk.CONSTRAINT_NAME    =a.INDEX_NAME" + "  )" + " ORDER BY a.INDEX_NAME," + "  a.COLUMN_POSITION";
			break;

		case SqlServer:
			sql = "SELECT " + "CAST(idx.name as nvarchar(255)) IndexName, " + "CAST(object_name(idx_cols.object_id) as nvarchar(255)) TableName, " + "CAST(cols.name as nvarchar(255)) ColName " + "FROM " + "sys.indexes idx, " + "sys.index_columns idx_cols, " + "sys.columns cols "
					+ "WHERE " + "idx.index_id = idx_cols.index_id and " + "idx.object_id = idx_cols.object_id and " + "idx_cols.object_id = cols.object_id and " + "idx_cols.column_id = cols.column_id and " + "idx.is_primary_key = 0" + "idx.is_unique = " + (unique ? 1 : 0)
					+ " and " + "lower(object_name(idx_cols.object_id)) LIKE lower('" + tableLike + "') " + "ORDER BY " + "TableName, IndexName, idx_cols.index_column_id";
			break;

		case Postgres:
			sql =
				"select " + "indexes.relname as index, " + "max(tables.relname) as table, " + "max(columns.attname) as column " + 
					"from " +
						"pg_class tables, " + "pg_class indexes, " + "pg_namespace owners, " + "pg_index root, " + "pg_attribute columns " +
					"where " +
						"tables.oid = root.indrelid and indexes.oid = root.indexrelid and owners.oid = tables.relnamespace and tables.oid = columns.attrelid and " +
						"columns.attnum > 0 and (columns.attnum = ANY(root.indkey) or root.indkey = '0' and root.indexprs is not null) and  tables.relkind = 'r' and " +
						"root.indisunique = '" + (unique ? "t" : "f") + "' and not root.indisprimary and owners.nspname = '" + ConnectionManager.database().schema() + "' " +
					"group by index";
			break;

		case H2:
			sql = "SELECT INDEX_NAME, TABLE_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.INDEXES"
					+ " WHERE " + (unique ? "NOT" : "") + " NON_UNIQUE AND CONSTRAINT_NAME IS NULL";
			break;

		default:
			throw new UnknownDatabaseException();
		}

		Map<String, Index> indexes = new Hashtable<String, Index>();

		Cursor cursor = SelectStatement.cursor(sql);

		while(cursor.next()) {
			String index = cursor.getString(1).get();
			String table = cursor.getString(2).get();
			String column = cursor.getString(3).get();

			if(!indexes.containsKey(index)) {
				indexes.put(index, new Index(index, table, column, unique));
			} else {
				indexes.get(index).fields.add(column);
			}
		}

		cursor.close();

		return indexes.values();
	}

	static private Iterable<ForeignKey> getForeignKeys(String tableLike) throws SQLException {
		String sql;

		switch(ConnectionManager.vendor()) {
		case Oracle:
			sql = "SELECT TO_NCHAR(pk.TABLE_NAME) PK_TABNAME," + " TO_NCHAR(pkCols.COLUMN_NAME) PK_COLNAME ," + " TO_NCHAR(fk.TABLE_NAME) FK_TABNAME      ," + " TO_NCHAR(fkCols.COLUMN_NAME) FK_COLNAME ," + " TO_NCHAR(fk.CONSTRAINT_NAME) CONSTRAINT_NAME" + "  FROM"
					+ " (SELECT CONSTRAINT_NAME," + "   constraint_type      ," + "   TABLE_NAME           ," + "   R_CONSTRAINT_NAME" + "    FROM user_constraints a" + "   WHERE constraint_type = 'R'" + " AND lower(TABLE_NAME) LIKE lower('" + tableLike + "')"
					+ " ) fk                   ," + " (SELECT CONSTRAINT_NAME," + "   constraint_type      ," + "   TABLE_NAME           ," + "   R_CONSTRAINT_NAME" + "    FROM user_constraints a" + "   WHERE constraint_type = 'P'" + " ) pk                    ,"
					+ " user_cons_columns fkCols," + " user_cons_columns pkCols" + " WHERE fk.R_CONSTRAINT_NAME = pk.CONSTRAINT_NAME" + " AND fk.CONSTRAINT_NAME       = fkCols.CONSTRAINT_NAME" + " AND pk.CONSTRAINT_NAME       = pkCols.CONSTRAINT_NAME";
			break;

		case SqlServer:
			sql = "select CAST(pk.TABLE_NAME as nvarchar(255)) PK_TABNAME, CAST(pk.COLUMN_NAME as nvarchar(255)) PK_COLNAME, CAST(fk.TABLE_NAME as nvarchar(255)) FK_TABNAME, CAST(fk.COLUMN_NAME as nvarchar(255)) FK_COLNAME, CAST(fk.CONSTRAINT_NAME as nvarchar(255)) CONSTRAINT_NAME "
					+ " from INFORMATION_SCHEMA.KEY_COLUMN_USAGE fk, " + " INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS refc, INFORMATION_SCHEMA.KEY_COLUMN_USAGE pk " + " where fk.CONSTRAINT_NAME=refc.CONSTRAINT_NAME and refc.UNIQUE_CONSTRAINT_NAME=pk.CONSTRAINT_NAME "
					+ " and lower(fk.TABLE_NAME) like lower('" + tableLike + "')";
			break;

		case Postgres:
			sql = "select " + "pk.table_name, " + "pk.column_name, " + "fk.table_name, " + "fk.column_name, " + "fk.constraint_name " + "from " + "information_schema.key_column_usage fk, " + "information_schema.referential_constraints refc, "
					+ "information_schema.key_column_usage pk " + "where " + "fk.constraint_name = refc.constraint_name and " + "refc.unique_constraint_name = pk.constraint_name and " + "fk.constraint_schema = '" + ConnectionManager.database().schema() + "'";
			break;

		case H2:
			sql = "select pktable_name, pkcolumn_name, fktable_name, fkcolumn_name, fk_name from information_schema.cross_references"
					+ " where fktable_schema = '" + ConnectionManager.database().schema() + "'";
			break;

		default:
			throw new UnknownDatabaseException();
		}

		Collection<ForeignKey> foreignKeys = new LinkedList<ForeignKey>();

		Cursor cursor = SelectStatement.cursor(sql);

		while(cursor.next()) {
			foreignKeys.add(new ForeignKey(cursor.getString(1).get(), cursor.getString(2).get(), cursor.getString(3).get(), cursor.getString(4).get(), cursor.getString(5).get()));
		}

		cursor.close();

		return foreignKeys;
	}
}
