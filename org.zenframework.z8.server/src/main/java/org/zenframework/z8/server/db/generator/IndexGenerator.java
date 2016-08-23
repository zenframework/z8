package org.zenframework.z8.server.db.generator;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.BasicSelect;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.Cursor;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.Statement;
import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.runtime.CLASS;

class IndexGenerator {
	private Table table;
	private Field field;
	private int id;
	private boolean unique;

	IndexGenerator(Table table, Field field, int id, boolean unique) {
		this.table = table;
		this.field = field;
		this.id = id;
		this.unique = unique;
	}

	void run(Connection connection) throws SQLException {
		Database database = connection.database();
		DatabaseVendor vendor = database.vendor();

		String sql = "CREATE " + (unique ? "UNIQUE " : "") + "INDEX " + vendor.quote((unique ? "UNQ_" : "IDX_") + table.name() + "_" + id) + " " + "ON " + database.tableName(table.name()) + "(" + formatIndexFields(vendor) + ")" + getTableSpace(connection);

		Statement.executeUpdate(connection, sql);
	}

	private String formatIndexFields(DatabaseVendor vendor) {
		String result = vendor.quote(this.field.name());

		for(Field field : CLASS.asList(this.field.indexFields))
			result += ", " + vendor.quote(field.name());

		return result;
	}

	static void dropIndex(Connection conn, String tableName, String indexName) throws SQLException {
		switch(conn.vendor()) {
		case Postgres:
		case Oracle:
			dropIndexOracle(conn, tableName, indexName);
			break;
		case SqlServer:
			dropIndexSQLServer(conn, tableName, indexName);
			break;
		default:
			assert (false);
		}
	}

	private static void dropIndexOracle(Connection connection, String tableName, String indexName) throws SQLException {
		Database database = connection.database();
		String sql = "drop index " + database.tableName(indexName);
		Statement.executeUpdate(connection, sql);
	}

	private static void dropIndexSQLServer(Connection connection, String tableName, String indexName) throws SQLException {
		DatabaseVendor vendor = connection.vendor();
		String sql = "drop index " + vendor.quote(tableName) + "." + vendor.quote(indexName);
		Statement.executeUpdate(connection, sql);
	}

	static private Map<String, String> tableSpaces = new HashMap<String, String>();

	public String getTableSpace(Connection connection) throws SQLException {
		String tableSpace = tableSpaces.get(connection.schema());

		if(tableSpace == null) {
			tableSpace = getTableSpace(connection, "INDEX", "IDX");
			tableSpaces.put(connection.schema(), tableSpace);
		}

		return tableSpace;
	}

	static public String getTableSpace(Connection connection, String dataFile1, String dataFile2) throws SQLException {
		String sql = null;
		String result = "";

		switch(connection.vendor()) {
		case Oracle: {
			sql = "SELECT TO_NCHAR(q.tablespace_name) AS TBS FROM user_ts_quotas q inner join user_tablespaces t on (q.tablespace_name=t.tablespace_name) WHERE (upper(q.tablespace_name) like '%" + dataFile1 + "'  ESCAPE '/' or  upper(q.tablespace_name) like '%" + dataFile2
					+ "' ESCAPE '/') AND q.max_bytes<>0";
			break;
		}
		case SqlServer: {
			sql = "select Convert(nvarchar(max), groupname) as TBS from sysfilegroups where (upper(groupname) like '%" + dataFile1 + "' or upper(groupname) like '%" + dataFile2 + "') and (status & 8 <> 8)";
			break;
		}
		default: {
			return result;
		}
		}

		Cursor cursor = BasicSelect.cursor(connection, sql);
		result = cursor.next() ? cursor.getString(1).get() : "";
		cursor.close();

		if(result.isEmpty()) {
			return result;
		}

		switch(connection.vendor()) {
		case Oracle:
			return " TABLESPACE " + result;
		case SqlServer:
			return " ON " + result;
		default:
			throw new UnknownDatabaseException();
		}
	}
}
