package org.zenframework.z8.server.db.generator;

import java.sql.SQLException;

import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.DmlStatement;
import org.zenframework.z8.server.engine.Database;

class ForeignKeyGenerator {
	private ForeignKey foreignKey;

	ForeignKeyGenerator(ForeignKey foreignKey) {
		this.foreignKey = foreignKey;
	}

	void run() throws SQLException {
		Connection connection = ConnectionManager.get();
		Database database = connection.getDatabase();
		DatabaseVendor vendor = database.getVendor();

		String sql = "ALTER TABLE " + database.getTableName(foreignKey.table) + " " + "ADD CONSTRAINT " + vendor.quote(foreignKey.name) + " " + "FOREIGN KEY" + "(" + vendor.quote(foreignKey.field) + ")" + " " + "REFERENCES " + database.getTableName(foreignKey.referenceTable) + " "
				+ "(" + vendor.quote(foreignKey.referenceField) + ")";

		DmlStatement.execute(sql);
	}
}
