package org.zenframework.z8.server.db;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BasicSelect extends BasicStatement {
    public BasicSelect(Connection connection) {
        super(connection);
    }

    @Override
    public void prepare(String sql) throws SQLException {
        this.sql = sql;
        this.statement = connection().prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    public Cursor execute() throws SQLException {
        return new Cursor(this);
    }

    public static Cursor cursor(Connection connection, String sql) throws SQLException {
        BasicSelect select = new BasicSelect(connection);
        select.prepare(sql);
        return select.execute();
    }
}
