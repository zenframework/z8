package org.zenframework.z8.server.db;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.TimestampField;
import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.server.types.geometry;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public abstract class Statement {
	private Connection connection;

	private String sql;
	private int priority;
	private PreparedStatement preparedStatement;

	protected abstract PreparedStatement createPreparedStatement(Connection connection, String sql) throws SQLException;
	protected abstract void prepare() throws SQLException;

	protected Statement(Connection connection, String sql, int priority) {
		this.connection = connection != null ? connection : ConnectionManager.get();
		this.sql = sql;
		this.priority = priority;
	}

	@Override
	public int hashCode() {
		return sql.hashCode();
	}

	public Connection getConnection() {
		return connection;
	}

	public Database getDatabase() {
		return getConnection().getDatabase();
	}

	public DatabaseVendor getVendor() {
		return getConnection().getVendor();
	}

	public encoding getCharset() {
		return getConnection().getCharset();
	}

	public String getSql() {
		return sql;
	}

	public int getPriority() {
		return priority;
	}

	public PreparedStatement preparedStatement() throws SQLException {
		return preparedStatement == null ? preparedStatement = createPreparedStatement(connection, sql) : preparedStatement;
	}

	public ResultSet executeQuery() throws SQLException {
		prepare();
		return connection.executeQuery(this);
	}

	public int executeUpdate() throws SQLException {
		prepare();
		return connection.executeUpdate(this);
	}

	public void executeCall() throws SQLException {
		prepare();
		connection.executeCall(this);
	}

	public void addBatch() throws SQLException {
		preparedStatement().addBatch();
	}

	public void executeBatch() throws SQLException {
		preparedStatement().executeBatch();
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

	public void close() {
		if(preparedStatement == null)
			return;

		try {
			preparedStatement.close();
		} catch(SQLException e) {
			Trace.logError(e);
		} finally {
			preparedStatement = null;
		}
	}

	protected void setNull(int position) throws SQLException {
		preparedStatement().setNull(position, Types.NULL);
	}

	protected void setBinary(int position, binary value) throws SQLException {
		try {
			InputStream stream = value.get();
			preparedStatement().setBinaryStream(position, stream, stream.available());
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected void setBoolean(int position, bool value) throws SQLException {
		switch(getVendor()) {
		case Postgres:
			preparedStatement().setInt(position, value.get() ? 1 : 0);
			break;
		default:
			preparedStatement().setBoolean(position, value.get());
		}
	}

	protected void setDate(int position, date value) throws SQLException {
		preparedStatement().setLong(position, value.getTicks());
	}

	protected void setTimestamp(int position, date value) throws SQLException {
		preparedStatement().setDate(position, new Date(value.getTicks()));
	}

	protected void setDatespan(int position, datespan value) throws SQLException {
		preparedStatement().setLong(position, value.get());
	}

	protected void setDecimal(int position, decimal value) throws SQLException {
		double d = value.getDouble();

		if(Math.abs(d) < 0.1)
			preparedStatement().setDouble(position, d);
		else
			preparedStatement().setBigDecimal(position, value.get());
	}

	protected void setGeometry(int position, geometry value) throws SQLException {
		InputStream stream = value.stream();
		if(stream != null)
			preparedStatement().setBinaryStream(position,  stream);
		else
			setNull(position);
	}

	protected void setGuid(int position, guid value) throws SQLException {
		switch(getVendor()) {
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
			preparedStatement().setBytes(position, b);
			break;
		}
		case Postgres:
			preparedStatement().setObject(position, value.get());
			break;
		default:
			preparedStatement().setString(position, value.toString(true));
		}
	}

	protected void setInteger(int position, integer value) throws SQLException {
		preparedStatement().setLong(position, value.get());
	}

	protected void setString(int position, string value) throws SQLException {
		preparedStatement().setString(position, value.get());
	}

	protected void setText(int position, string value) throws SQLException {
		setBinary(position, new binary(value.getBytes(getCharset())));
	}

	private boolean processNull(int position, primary value) throws SQLException {
		if(value != null)
			return false;

		setNull(position);
		return true;
	}

	public void set(int position, Field field, primary value) throws SQLException {
		if(processNull(position, value))
			return;

		FieldType type = field.type();

		switch(type) {
		case Date:
		case Datetime:
			if(field instanceof TimestampField)
				setTimestamp(position, (date)value);
			else
				setDate(position, (date)value);
			break;
		default:
			set(position, type, value);
		};
	}

	public void set(int position, FieldType type, primary value) throws SQLException {
		if(processNull(position, value))
			return;

		switch(type) {
		case Attachments:
		case File:
		case Text:
			setText(position, (string)value);
			break;
		case Binary:
			setBinary(position, (binary)value);
			break;
		case Boolean:
			setBoolean(position, (bool)value);
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
		case Geometry:
			setGeometry(position, (geometry)value);
			break;
		case Guid:
			setGuid(position, (guid)value);
			break;
		case Integer:
			setInteger(position, (integer)value);
			break;
		case String:
			setString(position, (string)value);
			break;
		case Null:
			setNull(position);
			break;
		default:
			throw new RuntimeException("Unknown data type: '" + type + "'");
		}
	}
}
