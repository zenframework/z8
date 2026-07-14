package org.zenframework.z8.server.db.dialect;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.engine.IDatabase;
import org.zenframework.z8.server.utils.StringUtils;

public class OracleDialect extends DatabaseDialect {

	public static final String Name = "Oracle";

	@Override
	public String name() {
		return Name;
	}

	@Override
	public DatabaseVendor vendor() {
		return DatabaseVendor.Oracle;
	}

	@Override
	public String getTables(IDatabase database) {
		return "SELECT " + 
				"TABLE_NAME, " +
				"COLUMN_NAME, " +
				"DATA_TYPE, " +
				"COALESCE(CHAR_COL_DECL_LENGTH, DATA_PRECISION, 0), " +
				"COALESCE(DATA_SCALE, 0), " + 
				"CASE WHEN NULLABLE = 'NO' THEN 0 ELSE 1 END, " +
				"NVL(DATA_DEFAULT, '') " +
			"FROM ALL_TAB_COLUMNS " +
			"WHERE OWNER = '" + database.schema() + "' " +
			"ORDER BY TABLE_NAME, COLUMN_ID";
	}

	@Override
	public String getPrimaryKeys(IDatabase database) {
		return "SELECT TO_NCHAR(pkCols.CONSTRAINT_NAME)," + "  TO_NCHAR(pkCols.TABLE_NAME)," + "  TO_NCHAR(pkCols.COLUMN_NAME)" + " FROM user_constraints pk," + "  user_cons_columns pkCols" + " WHERE pk.constraint_type = 'P'"
				+ "  AND pk.CONSTRAINT_NAME     =pkCols.CONSTRAINT_NAME"
				// + "  AND lower(pkCols.TABLE_NAME) LIKE lower('%')"
				+ " ORDER BY pkCols.TABLE_NAME," + "  pkCols.POSITION";
	}

	@Override
	public String getForeignKeys(IDatabase database) {
		return "SELECT TO_NCHAR(pk.TABLE_NAME) PK_TABNAME," + " TO_NCHAR(pkCols.COLUMN_NAME) PK_COLNAME ," + " TO_NCHAR(fk.TABLE_NAME) FK_TABNAME      ," + " TO_NCHAR(fkCols.COLUMN_NAME) FK_COLNAME ," + " TO_NCHAR(fk.CONSTRAINT_NAME) CONSTRAINT_NAME" + "  FROM"
				+ " (SELECT CONSTRAINT_NAME," + "   constraint_type      ," + "   TABLE_NAME           ," + "   R_CONSTRAINT_NAME" + "    FROM user_constraints a" + "   WHERE constraint_type = 'R'" // + " AND lower(TABLE_NAME) LIKE lower('%')"
				+ " ) fk                   ," + " (SELECT CONSTRAINT_NAME," + "   constraint_type      ," + "   TABLE_NAME           ," + "   R_CONSTRAINT_NAME" + "    FROM user_constraints a" + "   WHERE constraint_type = 'P'" + " ) pk                    ,"
				+ " user_cons_columns fkCols," + " user_cons_columns pkCols" + " WHERE fk.R_CONSTRAINT_NAME = pk.CONSTRAINT_NAME" + " AND fk.CONSTRAINT_NAME       = fkCols.CONSTRAINT_NAME" + " AND pk.CONSTRAINT_NAME       = pkCols.CONSTRAINT_NAME";
	}

	@Override
	public String getIndixes(IDatabase database) {
		return "SELECT TO_NCHAR(a.INDEX_NAME) INDEX_NAME, TO_NCHAR(a.TABLE_NAME) TABLE_NAME, TO_NCHAR(a.COLUMN_NAME) COLUMN_NAME, TO_NCHAR(b.UNIQUENESS) FROM user_ind_columns a,"
				+ " (SELECT a.INDEX_NAME, a.UNIQUENESS FROM user_indexes a"
				// + " WHERE UNIQUENESS = '" + (unique ? "UNIQUE" : "NONUNIQUE") + "'"
				// + " AND lower(a.table_name) LIKE lower('%')"
				+ ") b"
				+ " WHERE a.INDEX_NAME = b.INDEX_NAME AND NOT EXISTS (SELECT 0 FROM user_constraints pk WHERE pk.constraint_type = 'P'"
				+ " AND pk.CONSTRAINT_NAME = a.INDEX_NAME) ORDER BY a.INDEX_NAME, a.COLUMN_POSITION";
	}

	public String formatSqlName(String name) {
		return name.length() > 15 ? StringUtils.translit(name, 30) : name;
	}

	@Override
	public String formatDefaultValue(String defaultValue, String typeName) {
		return defaultValue.replace("::" + typeName, "");
	}

	@Override
	public String formatType(FieldType type) {
		switch(type) {
		case Attachments:
		case Binary:
		case File:
		case Text:
			return "BLOB";
		case Boolean:
			return "NUMBER";
		case Date:
		case Datetime:
		case Datespan:
		case Integer:
		case Decimal:
			return "NUMBER";
		case Geometry:
			throw new RuntimeException("Unsupported data type: '" + type.toString() + "'");
		case Guid:
			return "RAW";
		case String:
			return "NVARCHAR2";
		default:
			throw new RuntimeException("Unknown data type: '" + type.toString() + "'");
		}
	}

	@Override
	public String formatSqlType(FieldType type, Object... params) {
		String result = formatType(type);

		switch(type) {
		case Attachments:
		case Binary:
		case File:
		case Text:
			return result + "(MAX)";

		case Boolean:
			return result + "(1)";

		case Integer:
		case Datespan:
			return result + "(19, 0)";

		case Guid:
			return result + "(16)";

		default:
			return result;
		}
	}
}
