package org.zenframework.z8.server.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.types.encoding;

public interface IStatement {
    public Connection connection();

    public Database database();
    public DatabaseVendor vendor();

    public encoding charset();

    public String sql();

    public ResultSet executeQuery() throws SQLException;

    public int executeUpdate() throws SQLException;

    public void close();
}
