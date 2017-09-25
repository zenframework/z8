package org.zenframework.z8.server.db;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import org.zenframework.z8.server.base.table.value.BinaryField;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.DateField;
import org.zenframework.z8.server.base.table.value.DatespanField;
import org.zenframework.z8.server.base.table.value.DecimalField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.base.table.value.TextField;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datespan;
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

	public boolean isClosed() {
		return resultSet == null;
	}

	public void close() {
		if(isClosed())
			return;

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

	public guid getGuid(int position) throws SQLException {
		return getGuid(position, new GuidField(null));
	}

	private guid getGuid(int position, Field field) throws SQLException {
		Object value = null;

		if(statement.vendor() == DatabaseVendor.Oracle)
			value = resultSet.getString(position);
		else if(statement.vendor() == DatabaseVendor.Postgres)
			value = resultSet.getObject(position);

		boolean wasNull = value == null || wasNull();
		field.setWasNull(wasNull);

		return !wasNull ? (value instanceof UUID ? new guid((UUID)value) : new guid((String)value)) : new guid();
	}

	public bool getBoolean(int position) throws SQLException {
		return getBoolean(position, new BoolField(null));
	}

	private bool getBoolean(int position, Field field) throws SQLException {
		boolean value = resultSet.getBoolean(position);
		boolean wasNull = wasNull();
		field.setWasNull(wasNull);
		return !wasNull ? new bool(value) : bool.False;
	}

	public integer getInteger(int position) throws SQLException {
		return getInteger(position, new IntegerField(null));
	}

	private integer getInteger(int position, Field field) throws SQLException {
		long value = resultSet.getLong(position);
		boolean wasNull = wasNull();
		field.setWasNull(wasNull);
		return !wasNull ? new integer(value) : integer.Zero;
	}

	public string getString(int position) throws SQLException {
		return getString(position, new StringField(null));
	}

	private string getString(int position, Field field) throws SQLException {
		Object value = null;

		if(statement.vendor() == DatabaseVendor.Oracle)
			value = field.type() == FieldType.String ? resultSet.getString(position) : resultSet.getBytes(position);
		else if(statement.vendor() == DatabaseVendor.Postgres)
			value = resultSet.getBytes(position);

		boolean wasNull = value == null || wasNull();
		field.setWasNull(wasNull);

		return !wasNull ? (value instanceof String ? new string((String)value) : new string((byte[])value, statement.charset())) : new string();
	}

	public string getText(int position) throws SQLException {
		return getText(position, new TextField(null));
	}

	private string getText(int position, Field field) throws SQLException {
		return getString(position, field);
	}

	public date getDate(int position) throws SQLException {
		return getDate(position, new DateField(null));
	}

	private date getDate(int position, Field field) throws SQLException {
		Object value = null;

		if(statement.vendor() == DatabaseVendor.Oracle)
			value = resultSet.getLong(position);
		else
			value = resultSet.getTimestamp(position);

		boolean wasNull = value == null || wasNull();
		field.setWasNull(wasNull);

		return !wasNull ? new date(value instanceof Timestamp ? (Timestamp)value : new Timestamp((Long)value)) : date.Min;
	}

	public datespan getDatespan(int position) throws SQLException {
		return getDatespan(position, new DatespanField(null));
	}

	private datespan getDatespan(int position, Field field) throws SQLException {
		return new datespan(getInteger(position, field));
	}

	public decimal getDecimal(int position) throws SQLException {
		return getDecimal(position, new DecimalField(null));
	}

	private decimal getDecimal(int position, Field field) throws SQLException {
		BigDecimal value = resultSet.getBigDecimal(position);
		boolean wasNull = value == null || wasNull();
		field.setWasNull(wasNull);
		return !wasNull ? new decimal(value) : decimal.Zero;
	}

	public binary getBinary(int position) throws SQLException {
		return getBinary(position, new BinaryField(null));
	}

	private binary getBinary(int position, Field field) throws SQLException {
		InputStream value = resultSet.getBinaryStream(position);
		boolean wasNull = value == null || wasNull();
		field.setWasNull(wasNull);
		return !wasNull ? new binary(value) : new binary();
	}

	public primary get(Field field) throws SQLException {
		FieldType type = field.type();
		int position = field.position + 1;

		switch(type) {
		case Guid:
			return getGuid(position, field);
		case Boolean:
			return getBoolean(position, field);
		case Integer:
			return getInteger(position, field);
		case Text:
		case Attachments:
			return getText(position, field);
		case String:
			return getString(position, field);
		case Date:
		case Datetime:
			return getDate(position, field);
		case Datespan:
			return getDatespan(position, field);
		case Decimal:
			return getDecimal(position, field);
		case Binary:
			return getBinary(position, field);
		default:
			throw new UnsupportedOperationException();
		}
	}
}
