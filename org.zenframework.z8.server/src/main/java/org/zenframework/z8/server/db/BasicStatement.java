package org.zenframework.z8.server.db;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public abstract class BasicStatement implements IStatement {
	private Connection connection;

	protected String sql;
	protected int priority;
	protected PreparedStatement statement;

	public abstract void prepare(String sql, int priority) throws SQLException;

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

	public void prepare(String sql) throws SQLException {
		prepare(sql, 0);
	}

	public void prepare() throws SQLException {
		prepare(sql, priority);
	}

	@Override
	public ResultSet executeQuery() throws SQLException {
		return connection.executeQuery(this);
	}

	@Override
	public int executeUpdate() throws SQLException {
		return connection.executeUpdate(this);
	}

	@Override
	protected void finalize() throws Throwable {
		close();
	}

	public void safeClose() {
		try {
			close();
		} catch(Throwable e) {
		}
	}

	@Override
	synchronized public void close() {
		try {
			if(statement != null && !statement.isClosed() && !connection().inBatchMode())
				cleanup();
		} catch(SQLException e) {
			Trace.logError(e);
		}
	}

	protected void cleanup() throws SQLException {
		if(!connection().inTransaction()) {
			statement.close();
			statement = null;
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
			statement.setObject(position, value.get());
			break;
		default:
			statement.setString(position, value.toString(true));
		}
	}

	public void setBoolean(int position, bool value) throws SQLException {
		value = value != null ? value : bool.False;

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
		statement.setLong(position, value != null ? value.getTicks() : date.UtcMin);
	}

	public void setDatespan(int position, datespan value) throws SQLException {
		setInteger(position, new integer(value.get()));
	}

	public void setDecimal(int position, decimal value) throws SQLException {
		value = value != null ? value : decimal.zero();

		double d = value.getDouble();

		if(Math.abs(d) < 0.1)
			statement.setDouble(position, d);
		else
			statement.setBigDecimal(position, value.get());
	}

	public void setBinary(int position, binary value) throws SQLException {
		try {
			value = value != null ? value : new binary();
			InputStream stream = value.get();

			long size = stream.available();

			statement.setBinaryStream(position, stream, size);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void set(int position, FieldType type, primary value) throws SQLException {
		switch(type) {
		case Guid:
			setGuid(position, (guid)value);
			break;
		case Boolean:
			setBoolean(position, (bool)value);
			break;
		case Integer:
			setInteger(position, (integer)value);
			break;
		case String:
			setString(position, (string)value);
			break;
		case Date:
		case Datetime:
			setDate(position, (date)value);
			break;
		case Datespan:
			setDatespan(position, (datespan)value);
			break;
		case Decimal:
			setDecimal(position, (decimal)value);
			break;
		case Text:
		case Attachments:
			string string = value != null ? (string)value : new string();
			setBinary(position, new binary(string.getBytes(charset())));
			break;
		case Binary:
			setBinary(position, (binary)value);
			break;
		case Null:
			setNull(position);
			break;
		default:
			throw new RuntimeException("Unknown data type: '" + type + "'");
		}
	}
}
