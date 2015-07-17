package org.zenframework.z8.server.db.generator;

import java.sql.SQLException;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.Statement;
import org.zenframework.z8.server.engine.Database;

class PrimaryKeyGenerator {
    private Table table = null;

    PrimaryKeyGenerator(Table table) {
        this.table = table;
    }

    void run(Connection connection) throws SQLException {
        Field primaryKey = table.primaryKey();

        if(primaryKey == null)
            return;

        Database database = connection.database();
        DatabaseVendor vendor = database.vendor();

        String sql = "ALTER TABLE " + database.tableName(table.name()) + " ADD PRIMARY KEY(" + vendor.quote(primaryKey.name())
                + ")";
        Statement.executeUpdate(connection, sql);
    }
}
