package org.zenframework.z8.server.db;

import java.io.InputStream;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public abstract class BasicStatement implements IStatement {
    private Connection connection;

    protected String sql;
    protected PreparedStatement statement;

    public abstract void prepare(String sql) throws SQLException;

    protected BasicStatement(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public Database database() {
        return connection().database();
    }

    @Override
    public DatabaseVendor vendor() {
        return connection().vendor();
    }

    @Override
    public encoding charset() {
        return connection().charset();
    }

    @Override
    public String sql() {
        return sql;
    }

    public PreparedStatement statement() {
        return statement;
    }

    public void prepare() throws SQLException {
        prepare(sql);
    }
    
    @Override
    public ResultSet executeQuery() throws SQLException {
        return connection.executeQuery(this);
    }

    @Override
    public void executeUpdate() throws SQLException {
        connection.executeUpdate(this);
    }

    @Override
    protected void finalize() throws Throwable {
        close();
    }
    
    public void safeClose() {
        try {
            close();
        } catch (Throwable e) {}
    }

    @Override
    public void close() {
        try {
            if(statement != null && !statement.isClosed()) {
                statement.close();
            }
        }
        catch(SQLException e) {
            Trace.logError(e);
        }
    }

    public void setNull(int position) throws SQLException {
        statement.setNull(position, Types.NULL);
    }

    public void setGuid(int position, guid value) throws SQLException {
        value = value != null ? value : new guid();

        switch(vendor()) {
        case Oracle: {
            byte[] b = new BigInteger(value.toString(false), 16).toByteArray();

            if(b.length < 16) {
                byte[] b1 = new byte[16];
                for(int i = 0; i < b1.length; i++) {
                    b1[i] = 0;
                }
                System.arraycopy(b, 0, b1, 16 - b.length, b.length);
                b = b1;
            }

            if(b.length > 16) {
                byte[] b1 = new byte[16];
                System.arraycopy(b, b.length - 16, b1, 0, 16);
                b = b1;
            }
            statement.setBytes(position, b);
            break;
        }
        case Postgres:
            statement.setObject(position, value.toUUID());
            break;
        default:
            statement.setString(position, value.toDbString(vendor()));
        }
    }

    public void setBoolean(int position, bool value) throws SQLException {
        value = value != null ? value : new bool();

        switch(vendor()) {
        case Postgres:
            statement.setInt(position, value.get() ? 1 : 0);
            break;
        default:
            statement.setBoolean(position, value.get());
        }
    }

    public void setInteger(int position, integer value) throws SQLException {
        statement.setLong(position, value != null ? value.get() : 0);
    }

    public void setString(int position, string value) throws SQLException {
        statement.setString(position, value != null ? value.get() : null);
    }

    public void setDate(int position, date value) throws SQLException {
        Timestamp timestamp = new Timestamp((value != null ? value : date.MIN).getTicks());
        statement.setTimestamp(position, timestamp);
    }

    public void setDatetime(int position, datetime value) throws SQLException {
        Timestamp timestamp = new Timestamp((value != null ? value : datetime.MIN).getTicks());
        statement.setTimestamp(position, timestamp);
    }

    public void setDatespan(int position, datespan value) throws SQLException {
        setInteger(position, new integer(value.get()));
    }

    public void setDecimal(int position, decimal value) throws SQLException {
        value = value != null ? value : new decimal();

        double d = value.getDouble();

        if(-0.1 < d && d < 0.1) {
            statement.setDouble(position, d);
        }
        else {
            statement.setBigDecimal(position, value.get());
        }
    }

    public void setBinary(int position, binary value) throws SQLException {
        value = value != null ? value : new binary();

        long size = value.getSize();
        InputStream stream = value.get();

        statement.setBinaryStream(position, stream, size);
    }
}
