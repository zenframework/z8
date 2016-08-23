package org.zenframework.z8.server.db;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Cursor {
	private IStatement statement;
	private ResultSet resultSet;

	public Cursor(IStatement statement) throws SQLException {
		this.statement = statement;
		this.resultSet = statement.executeQuery();
	}

	@Override
	protected void finalize() throws Throwable {
		close();
	}

	public boolean next() throws SQLException {
		return resultSet.next();
	}

	public boolean isAfterLast() throws SQLException {
		return resultSet.isAfterLast();
	}

	public void close() {
		try {
			if(resultSet != null) {
				resultSet.close();
				resultSet = null;
			}

			if(statement != null) {
				statement.close();
				statement = null;
			}
		} catch(SQLException e) {
			Trace.logError(e);
		}
	}

	public boolean wasNull() throws SQLException {
		return resultSet.wasNull();
	}

	public guid getGuid(int index) throws SQLException {
		Object value = resultSet.getObject(index);
		return value != null && !wasNull() ? (value instanceof UUID ? new guid((UUID)value) : new guid((String)value)) : new guid();
	}

	public bool getBoolean(int index) throws SQLException {
		boolean value = resultSet.getBoolean(index);
		return !wasNull() ? new bool(value) : new bool();
	}

	public integer getInteger(int index) throws SQLException {
		long value = resultSet.getLong(index);
		return !wasNull() ? new integer(value) : new integer();
	}

	public string getString(int index) throws SQLException {
		byte[] bytes = resultSet.getBytes(index);
		return bytes != null && !wasNull() ? new string(bytes, statement.charset()) : new string();
	}

	public string getText(int index) throws SQLException {
		return getString(index);
	}

	public string getStringNotNull(int index) throws SQLException {
		string value = getString(index);
		return value == null ? new string() : value;
	}

	public date getDate(int index) throws SQLException {
		Date value = resultSet.getDate(index);
		return value != null && !wasNull() ? new date(value) : date.MIN;
	}

	public datespan getDatespan(int index) throws SQLException {
		integer value = getInteger(index);
		return value != null ? new datespan(value) : null;
	}

	public datetime getDatetime(int index) throws SQLException {
		Timestamp value = resultSet.getTimestamp(index);
		return value != null && !wasNull() ? new datetime(value) : datetime.MIN;
	}

	public decimal getDecimal(int index) throws SQLException {
		BigDecimal value = resultSet.getBigDecimal(index);
		return value != null && !wasNull() ? new decimal(value) : new decimal();
	}

	public binary getBinary(int index) throws SQLException {
		InputStream value = resultSet.getBinaryStream(index);
		return value != null && !wasNull() ? new binary(value) : new binary();
	}

	public primary get(int index, FieldType fieldType) throws SQLException {
		switch(fieldType) {
		case Guid:
			return getGuid(index);
		case Boolean:
			return getBoolean(index);
		case Integer:
			return getInteger(index);
		case Text:
			return getText(index);
		case String:
			return getString(index);
		case Date:
			return getDate(index);
		case Datetime:
			return getDatetime(index);
		case Datespan:
			return getDatespan(index);
		case Decimal:
			return getDecimal(index);
		case Binary:
			return getBinary(index);
		default:
			assert (false);
			return null;
		}
	}
}
