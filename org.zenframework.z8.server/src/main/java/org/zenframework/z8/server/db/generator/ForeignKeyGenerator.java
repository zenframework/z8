package org.zenframework.z8.server.db.generator;

import java.sql.SQLException;

import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.Statement;
import org.zenframework.z8.server.engine.Database;

class ForeignKeyGenerator {
    private ForeignKey foreignKey;

    ForeignKeyGenerator(ForeignKey foreignKey) {
        this.foreignKey = foreignKey;
    }

    void run(Connection connection) throws SQLException {
        Database database = connection.database();
        DatabaseVendor vendor = database.vendor();

        String sql = "ALTER TABLE " + database.tableName(foreignKey.table) + " " + "ADD CONSTRAINT "
                + vendor.quote(foreignKey.name) + " " + "FOREIGN KEY" + "(" + vendor.quote(foreignKey.field) + ")" + " "
                + "REFERENCES " + database.tableName(foreignKey.referenceTable) + " " + "("
                + vendor.quote(foreignKey.referenceField) + ")";

        Statement.executeUpdate(connection, sql);
    }
}
