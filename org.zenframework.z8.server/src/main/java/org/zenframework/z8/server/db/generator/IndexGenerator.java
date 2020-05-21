package org.zenframework.z8.server.db.generator;

import java.sql.SQLException;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.Statement;
import org.zenframework.z8.server.engine.Database;

class IndexGenerator {
	private Table table;
	private Field field;
	private int index;

	IndexGenerator(Table table, Field field, int index) {
		this.table = table;
		this.field = field;
		this.index = index;
	}

	void run() throws SQLException {
		Connection connection = ConnectionManager.get();

		Database database = connection.database();
		DatabaseVendor vendor = database.vendor();

		boolean unique = field.unique();
		String sql = "create " + (unique ? "unique " : "") + "index " + vendor.quote((unique ? "Unq" : "Idx") + index + table.name()) + " " + "on " + database.tableName(table.name()) + " " + formatIndexField(vendor);

		Statement.executeUpdate(sql);
	}

	private String formatIndexField(DatabaseVendor vendor) {
		String name = vendor.quote(field.name());

		switch(field.type()) {
		case String:
			boolean trigram = field.trigram();
			return (trigram ? "using gist " : "") + "(" + name + (trigram ? " gist_trgm_ops" : "") + ")";
		case Geometry:
			return "using gist (" + name + ")";
		default:
			return "(" + name  + ")";
		}
	}

	static void dropIndex(String tableName, String indexName) throws SQLException {
		Connection connection = ConnectionManager.get();

		switch(connection.vendor()) {
		case Postgres:
		case Oracle:
		case H2:
			dropIndexOracle(connection, tableName, indexName);
			break;
		case SqlServer:
			dropIndexSQLServer(connection, tableName, indexName);
			break;
		default:
			throw new UnsupportedOperationException();
		}
	}

	private static void dropIndexOracle(Connection connection, String tableName, String indexName) throws SQLException {
		Database database = connection.database();
		String sql = "drop index " + database.tableName(indexName);
		Statement.executeUpdate(sql);
	}

	private static void dropIndexSQLServer(Connection connection, String tableName, String indexName) throws SQLException {
		DatabaseVendor vendor = connection.vendor();
		String sql = "drop index " + vendor.quote(tableName) + "." + vendor.quote(indexName);
		Statement.executeUpdate(sql);
	}
}
