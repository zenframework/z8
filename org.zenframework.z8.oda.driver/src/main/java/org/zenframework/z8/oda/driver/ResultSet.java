package org.zenframework.z8.oda.driver;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import org.eclipse.datatools.connectivity.oda.IBlob;
import org.eclipse.datatools.connectivity.oda.IClob;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.Select;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;

public class ResultSet implements IResultSet {
	public final static String NULL_STRING = "";

	private OdaQuery query;
	private Select cursor;

	boolean wasNull = false;
	boolean eof = false;

	public ResultSet(OdaQuery query) throws OdaException {
		this.query = query;
		cursor = query.getCursor();
	}

	@Override
	public IResultSetMetaData getMetaData() throws OdaException {
		return new ResultSetMetaData(query.getFields(), query.getQueryId());
	}

	@Override
	public void setMaxRows(int max) throws OdaException {
	}

	protected int getMaxRows() {
		return 0;
	}

	@Override
	public boolean next() throws OdaException {
		return cursor != null ? cursor.next() : false;
	}

	@Override
	public void close() throws OdaException {
		if(cursor != null) {
			cursor.close();
			cursor = null;
		}
	}

	@Override
	public int getRow() throws OdaException {
		return cursor.isAfterLast() ? -1 : 0;
	}

	private IField getColumn(int index) {
		return query.getFields().get(index - 1);
	}

	private primary getValue(int index) {
		return getColumn(index).get();
	}

	@Override
	public String getString(int index) throws OdaException {
		primary value = getValue(index);

		wasNull = value == null;

		if(wasNull)
			return NULL_STRING;

		if(value instanceof date) {
			date dt = (date)value;

			if(dt.equals(date.Min) || dt.equals(date.Max))
				return NULL_STRING;
		} else if(value instanceof bool)
			return ((bool)value).get() ? Resources.get("bool.true") : Resources.get("bool.false");

		return value.toString();
	}

	@Override
	public String getString(String columnName) throws OdaException {
		return getString(findColumn(columnName));
	}

	@Override
	public int getInt(int index) throws OdaException {
		primary value = getValue(index);

		wasNull = value == null;

		if(wasNull)
			return 0;

		if(value instanceof bool)
			return ((bool)value).get() ? 1 : 0;
		else if(value instanceof integer)
			return ((integer)value).getInt();

		throw new UnsupportedOperationException();
	}

	@Override
	public int getInt(String columnName) throws OdaException {
		return getInt(findColumn(columnName));
	}

	@Override
	public double getDouble(int index) throws OdaException {
		primary value = getValue(index);

		if(wasNull)
			return Double.NaN;

		if(value instanceof integer)
			return ((integer)value).get();
		else if(value instanceof decimal)
			return ((decimal)value).getDouble();

		throw new UnsupportedOperationException();
	}

	@Override
	public double getDouble(String columnName) throws OdaException {
		return getDouble(findColumn(columnName));
	}

	@Override
	public BigDecimal getBigDecimal(int index) throws OdaException {
		primary value = getValue(index);

		wasNull = value == null;

		if(wasNull)
			return BigDecimal.ZERO;

		if(value instanceof integer)
			return new BigDecimal(((integer)value).get());
		else if(value instanceof decimal)
			return ((decimal)value).get();

		throw new UnsupportedOperationException();
	}

	@Override
	public BigDecimal getBigDecimal(String columnName) throws OdaException {
		return getBigDecimal(findColumn(columnName));
	}

	@Override
	public Date getDate(int index) throws OdaException {
		primary value = getValue(index);

		wasNull = value == null;

		if(wasNull)
			return new Date(0);

		if(value instanceof date) {
			date dt = (date)value;

			if(dt.equals(date.Min) || dt.equals(date.Max))
				wasNull = true;

			return new Date(dt.getTicks());
		}

		throw new UnsupportedOperationException();
	}

	@Override
	public Date getDate(String columnName) throws OdaException {
		return getDate(findColumn(columnName));
	}

	@Override
	public Time getTime(int index) throws OdaException {
		primary value = getValue(index);

		wasNull = value == null;

		if(wasNull)
			return new Time(0);

		if(value instanceof date) {
			date dt = (date)value;

			if(dt.equals(date.Min) || dt.equals(date.Max))
				wasNull = true;

			return new Time(dt.getTicks());
		}

		throw new UnsupportedOperationException();
	}

	@Override
	public Time getTime(String columnName) throws OdaException {
		return getTime(findColumn(columnName));
	}

	@Override
	public Timestamp getTimestamp(int index) throws OdaException {
		primary value = getValue(index);

		wasNull = value == null;

		if(wasNull)
			return new Timestamp(0);

		if(value instanceof date) {
			date dt = (date)value;

			if(dt.equals(date.Min) || dt.equals(date.Max))
				wasNull = true;

			return new Timestamp(dt.getTicks());
		}

		throw new UnsupportedOperationException();
	}

	@Override
	public Timestamp getTimestamp(String columnName) throws OdaException {
		return getTimestamp(findColumn(columnName));
	}

	@Override
	public IBlob getBlob(int index) throws OdaException {
		final binary value = (binary)getValue(index);

		wasNull = value == null;

		if(wasNull)
			return null;

		return new IBlob() {
			@Override
			public InputStream getBinaryStream() throws OdaException {
				return value.get();
			}

			@Override
			public byte[] getBytes(long offset, int length) throws OdaException {
				throw new UnsupportedOperationException();
			}

			@Override
			public long length() throws OdaException {
				try {
					return getBinaryStream().available();
				} catch(IOException e) {
					throw new OdaException(e);
				}
			}
		};
	}

	@Override
	public IBlob getBlob(String columnName) throws OdaException {
		return getBlob(findColumn(columnName));
	}

	@Override
	public IClob getClob(int index) throws OdaException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IClob getClob(String columnName) throws OdaException {
		return getClob(findColumn(columnName));
	}

	@Override
	public boolean getBoolean(int index) throws OdaException {
		primary value = getValue(index);

		wasNull = value == null;

		if(wasNull)
			return false;

		if(value instanceof bool)
			return ((bool)value).get();

		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getBoolean(String columnName) throws OdaException {
		return getBoolean(findColumn(columnName));
	}

	@Override
	public boolean wasNull() throws OdaException {
		return wasNull;
	}

	@Override
	public int findColumn(String columnName) throws OdaException {
		int index = 1;
		for(IField field : query.getFields()) {
			if(columnName.equals(field.id()))
				return index;
			index++;
		}
		return 0;
	}

	@Override
	public Object getObject(int index) throws OdaException {
		return null;
	}

	@Override
	public Object getObject(String columnName) throws OdaException {
		return null;
	}
}