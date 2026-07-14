package org.zenframework.z8.server.db.dialect;

import java.sql.SQLException;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.engine.IDatabase;

public class SqlServerDialect extends DatabaseDialect {

	public static final String Name = "SqlServer";

	@Override
	public String name() {
		return Name;
	}

	@Override
	public DatabaseVendor vendor() {
		return DatabaseVendor.SqlServer;
	}

	public String quote(String name) {
		return '[' + name + ']';
	}

	@Override
	public String getTables(IDatabase database) {
		return "SELECT " + "cast(TABLE_NAME as nvarchar(max)), " + "cast(COLUMN_NAME as nvarchar(max)), " + "cast(DATA_TYPE as nvarchar(max)), " + "isnull(CHARACTER_MAXIMUM_LENGTH, isnull(NUMERIC_PRECISION, 0)) COLUMN_SIZE, " + "isnull(NUMERIC_SCALE, 0) DECIMAL_DIGITS, "
				+ "(case when IS_NULLABLE = 'NO' then 0 else 1 end) NULLABLE, " + "cast(isnull(COLUMN_DEFAULT, '') as nvarchar(max)) " + "FROM " + "INFORMATION_SCHEMA.COLUMNS "
				// + "WHERE " + "lower(TABLE_NAME) like lower('%')"
				+ "ORDER BY TABLE_NAME, ORDINAL_POSITION";
	}

	@Override
	public String getPrimaryKeys(IDatabase database) {
		return "select " + "CAST(pk.CONSTRAINT_NAME as nvarchar(255)), " + "CAST(pk.TABLE_NAME as nvarchar(255)), " + "CAST(pkCols.COLUMN_NAME as nvarchar(255)) " + "from " + "INFORMATION_SCHEMA.TABLE_CONSTRAINTS pk, " + "INFORMATION_SCHEMA.KEY_COLUMN_USAGE pkCols " + "where "
				+ "pk.CONSTRAINT_NAME = pkCols.CONSTRAINT_NAME and " + "pk.CONSTRAINT_TYPE = 'PRIMARY KEY'"
				// + " and " + "lower(pk.TABLE_NAME) like lower('%')"
				+ "order by " + "pk.TABLE_NAME, pkcols.ORDINAL_POSITION";
	}

	@Override
	public String getForeignKeys(IDatabase database) {
		return "select CAST(pk.TABLE_NAME as nvarchar(255)) PK_TABNAME, CAST(pk.COLUMN_NAME as nvarchar(255)) PK_COLNAME, CAST(fk.TABLE_NAME as nvarchar(255)) FK_TABNAME, CAST(fk.COLUMN_NAME as nvarchar(255)) FK_COLNAME, CAST(fk.CONSTRAINT_NAME as nvarchar(255)) CONSTRAINT_NAME "
				+ " from INFORMATION_SCHEMA.KEY_COLUMN_USAGE fk, " + " INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS refc, INFORMATION_SCHEMA.KEY_COLUMN_USAGE pk " + " where fk.CONSTRAINT_NAME=refc.CONSTRAINT_NAME and refc.UNIQUE_CONSTRAINT_NAME=pk.CONSTRAINT_NAME";
				// + " and lower(fk.TABLE_NAME) like lower('%')";
	}

	@Override
	public String getIndixes(IDatabase database) {
		return "SELECT CAST(idx.name as nvarchar(255)) IndexName, CAST(object_name(idx_cols.object_id) as nvarchar(255)) TableName, CAST(cols.name as nvarchar(255)) ColName, idx.is_unique UniqueIndex"
				+ " FROM sys.indexes idx, sys.index_columns idx_cols, sys.columns cols"
				+ " WHERE idx.index_id = idx_cols.index_id"
				+ " and idx.object_id = idx_cols.object_id"
				+ " and idx_cols.object_id = cols.object_id"
				+ " and idx_cols.column_id = cols.column_id and " + "idx.is_primary_key = 0"
				// + " and idx.is_unique = " + (unique ? 1 : 0)
				// + " and " + "lower(object_name(idx_cols.object_id)) LIKE lower('%')"
				+ " ORDER BY " + "TableName, IndexName, idx_cols.index_column_id";
	}

	@Override
	public String getRenameTable(IDatabase database, String oldName, String newName) {
		return "sp_rename " + formatTableName(database, oldName) + ", " + quote(newName);
	}

	@Override
	public String getDropIndex(IDatabase database, String tableName, String indexName) throws SQLException {
		return "drop index " + quote(tableName) + "." + quote(indexName);
	}

	@Override
	public String formatTableAlias(IDatabase database, String tableName, String alias) {
		return formatTableName(database, tableName) + " as " + alias;
	}

	@Override
	public int formatLength(int length, String typeName) {
		return typeName.equalsIgnoreCase("uniqueidentifier") ? 36 : length;
	}

	@Override
	public String formatType(FieldType type) {
		switch(type) {
		case Attachments:
		case Binary:
		case File:
		case Text:
			return "VARBINARY(MAX)";
		case Boolean:
			return "TINYINT";
		case Date:
		case Datetime:
		case Datespan:
		case Integer:
			return "BIGINT";
		case Decimal:
			return "NUMERIC";
		case Geometry:
			throw new RuntimeException("Unsupported data type: '" + type.toString() + "'");
		case Guid:
			return "UNIQUEIDENTIFIER";
		case String:
			return "NVARCHAR";
		default:
			throw new RuntimeException("Unknown data type: '" + type.toString() + "'");
		}
	}
}
