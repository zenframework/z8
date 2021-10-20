package org.zenframework.z8.server.db.generator;

import java.sql.SQLException;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.DmlStatement;
import org.zenframework.z8.server.engine.IDatabase;

class PrimaryKeyGenerator {
	private Table table = null;

	PrimaryKeyGenerator(Table table) {
		this.table = table;
	}

	void run() throws SQLException {
		Field primaryKey = table.primaryKey();

		if(primaryKey == null)
			return;

		Connection connection = ConnectionManager.get();
		IDatabase database = connection.database();
		DatabaseVendor vendor = database.vendor();

		String sql = "ALTER TABLE " + database.tableName(table.name()) + " ADD PRIMARY KEY(" + vendor.quote(primaryKey.name()) + ")";
		DmlStatement.execute(sql);
	}
}
