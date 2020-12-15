package org.zenframework.z8.server.db.generator;

import java.sql.SQLException;

import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.Statement;
import org.zenframework.z8.server.engine.IDatabase;

class ForeignKeyGenerator {
	private ForeignKey foreignKey;

	ForeignKeyGenerator(ForeignKey foreignKey) {
		this.foreignKey = foreignKey;
	}

	void run() throws SQLException {
		Connection connection = ConnectionManager.get();
		IDatabase database = connection.database();
		DatabaseVendor vendor = database.vendor();

		String sql = "ALTER TABLE " + database.tableName(foreignKey.table) + " " + "ADD CONSTRAINT " + vendor.quote(foreignKey.name) + " " + "FOREIGN KEY" + "(" + vendor.quote(foreignKey.field) + ")" + " " + "REFERENCES " + database.tableName(foreignKey.referenceTable) + " "
				+ "(" + vendor.quote(foreignKey.referenceField) + ")";

		Statement.executeUpdate(sql);
	}
}
