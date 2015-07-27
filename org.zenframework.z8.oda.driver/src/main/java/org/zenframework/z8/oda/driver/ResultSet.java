package org.zenframework.z8.oda.driver;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import org.eclipse.datatools.connectivity.oda.IBlob;
import org.eclipse.datatools.connectivity.oda.IClob;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.zenframework.z8.server.base.model.actions.ReadAction;
import org.zenframework.z8.server.base.model.sql.Select;
import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;

public class ResultSet implements IResultSet {
    public final static String NULL_STRING = "";

    private IValue[] columns = null;
    private Select cursor = null;

    boolean wasNull = false;
    boolean eof = false;

    public ResultSet(ReadAction action) throws OdaException {
        cursor = action.getCursor();
        columns = OdaQuery.getColumns(action);
    }

    @Override
    public IResultSetMetaData getMetaData() throws OdaException {
        return new ResultSetMetaData(columns);
    }

    @Override
    public void setMaxRows(int max) throws OdaException {}

    protected int getMaxRows() {
        return 0;
    }

    @Override
    public boolean next() throws OdaException {
        return cursor.next();
    }

    @Override
    public void close() throws OdaException {
        cursor.close();
        cursor = null;
    }

    @Override
    public int getRow() throws OdaException {
        return cursor.isAfterLast() ? -1 : 0;
    }

    @Override
    public String getString(int index) throws OdaException {
        primary value = columns[index - 1].get();

        wasNull = value == null;

        if(wasNull) {
            return NULL_STRING;
        }

        if(value instanceof date) {
            date d = (date)value;

            if(d.equals(date.MIN) || d.equals(date.MAX)) {
                return NULL_STRING;
            }
        }
        else if(value instanceof datetime) {
            datetime dt = (datetime)value;

            if(dt.equals(datetime.MIN) || dt.equals(datetime.MAX)) {
                return NULL_STRING;
            }
        }
        else if(value instanceof bool) {
            return ((bool)value).get() ? Resources.get("bool.true") : Resources.get("bool.false");
        }

        return value.toString();
    }

    @Override
    public String getString(String columnName) throws OdaException {
        return getString(findColumn(columnName));
    }

    @Override
    public int getInt(int index) throws OdaException {
        primary value = columns[index - 1].get();

        wasNull = value == null;

        if(wasNull) {
            return 0;
        }

        if(value instanceof bool) {
            return ((bool)value).get() ? 1 : 0;
        }
        else if(value instanceof integer) {
            return ((integer)value).getInt();
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public int getInt(String columnName) throws OdaException {
        return getInt(findColumn(columnName));
    }

    @Override
    public double getDouble(int index) throws OdaException {
        primary value = columns[index - 1].get();

        if(wasNull) {
            return Double.NaN;
        }

        if(value instanceof integer) {
            return ((integer)value).get();
        }
        else if(value instanceof decimal) {
            return ((decimal)value).getDouble();
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble(String columnName) throws OdaException {
        return getDouble(findColumn(columnName));
    }

    @Override
    public BigDecimal getBigDecimal(int index) throws OdaException {
        primary value = columns[index - 1].get();

        wasNull = value == null;

        if(wasNull) {
            return BigDecimal.ZERO;
        }

        if(value instanceof integer) {
            return new BigDecimal(((integer)value).get());
        }
        else if(value instanceof decimal) {
            return ((decimal)value).get();
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal getBigDecimal(String columnName) throws OdaException {
        return getBigDecimal(findColumn(columnName));
    }

    @Override
    public Date getDate(int index) throws OdaException {
        primary value = columns[index - 1].get();

        wasNull = value == null;

        if(wasNull) {
            return new Date(0);
        }

        if(value instanceof date) {
            date d = (date)value;

            if(d.equals(date.MIN) || d.equals(date.MAX)) {
                wasNull = true;
            }
            return new Date(d.getTicks());
        }
        else if(value instanceof datetime) {
            datetime dt = (datetime)value;

            if(dt.equals(datetime.MIN) || dt.equals(datetime.MAX)) {
                wasNull = true;
            }
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
        primary value = columns[index - 1].get();

        wasNull = value == null;

        if(wasNull) {
            return new Time(0);
        }

        if(value instanceof date) {
            date d = (date)value;

            if(d.equals(date.MIN) || d.equals(date.MAX)) {
                wasNull = true;
            }
            return new Time(d.getTicks());
        }
        else if(value instanceof datetime) {
            datetime dt = (datetime)value;

            if(dt.equals(datetime.MIN) || dt.equals(datetime.MAX)) {
                wasNull = true;
            }
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
        primary value = columns[index - 1].get();

        wasNull = value == null;

        if(wasNull) {
            return new Timestamp(0);
        }

        if(value instanceof date) {
            date d = (date)value;

            if(d.equals(date.MIN) || d.equals(date.MAX)) {
                wasNull = true;
            }
            return new Timestamp(d.getTicks());
        }
        else if(value instanceof datetime) {
            datetime dt = (datetime)value;

            if(dt.equals(datetime.MIN) || dt.equals(datetime.MAX)) {
                wasNull = true;
            }
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
        throw new UnsupportedOperationException();
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
        primary value = columns[index - 1].get();

        wasNull = value == null;

        if(wasNull) {
            return false;
        }

        if(value instanceof bool) {
            return ((bool)value).get();
        }

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
        for(int index = 0; index < columns.length; index++) {
            if(columnName.equals(columns[index].id())) {
                return index + 1;
            }
        }
        return 0;
    }

    @Override
    public Object getObject(int arg0) throws OdaException {
        return null;
    }

    @Override
    public Object getObject(String arg0) throws OdaException {
        return null;
    }
}
