package org.zenframework.z8.oda.driver;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.SortSpec;
import org.eclipse.datatools.connectivity.oda.spec.QuerySpecification;

import org.zenframework.z8.oda.driver.connection.Connection;
import org.zenframework.z8.server.base.model.actions.ReadAction;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IValue;

public class OdaQuery implements IQuery {
    private Connection connection = null;
    private ReadAction dataSet = null;
    private Map<Object, Object> context = null;

    public static IValue[] getColumns(Query data) throws OdaException {
        return getColumns(new ReadAction(data));
    }

    public static IValue[] getColumns(ReadAction action) throws OdaException {
        List<IValue> values = new ArrayList<IValue>();

        for(Field field : action.getSelectFields()) {
            if(!field.system.get()) {
                values.add(field);
            }
        }

        return values.toArray(new IValue[0]);
    }

    public OdaQuery(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void prepare(String queryText) throws OdaException {
        if(context != null) {
            dataSet = (ReadAction)context.get(queryText);
        }

        if(dataSet == null) {
            Query query = connection.getDataSet(queryText);
            dataSet = new ReadAction(query);
        }
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setAppContext(Object context) throws OdaException {
        this.context = (Map)context;
    }

    @Override
    public void close() throws OdaException {
        connection = null;
        dataSet = null;
        context = null;
    }

    @Override
    public IResultSetMetaData getMetaData() throws OdaException {
        return new ResultSetMetaData(getColumns(dataSet));
    }

    @Override
    public IResultSet executeQuery() throws OdaException {
        return new ResultSet(dataSet);
    }

    @Override
    public void setProperty(String name, String value) throws OdaException {}

    @Override
    public void setMaxRows(int max) throws OdaException {}

    @Override
    public int getMaxRows() throws OdaException {
        return 0;
    }

    @Override
    public void clearInParameters() throws OdaException {}

    @Override
    public void setInt(String parameterName, int value) throws OdaException {}

    @Override
    public void setInt(int parameterId, int value) throws OdaException {}

    @Override
    public void setDouble(String parameterName, double value) throws OdaException {}

    @Override
    public void setDouble(int parameterId, double value) throws OdaException {}

    @Override
    public void setBigDecimal(String parameterName, BigDecimal value) throws OdaException {}

    @Override
    public void setBigDecimal(int parameterId, BigDecimal value) throws OdaException {}

    @Override
    public void setString(String parameterName, String value) throws OdaException {}

    @Override
    public void setString(int parameterId, String value) throws OdaException {}

    @Override
    public void setDate(String parameterName, Date value) throws OdaException {}

    @Override
    public void setDate(int parameterId, Date value) throws OdaException {}

    @Override
    public void setTime(String parameterName, Time value) throws OdaException {}

    @Override
    public void setTime(int parameterId, Time value) throws OdaException {}

    @Override
    public void setTimestamp(String parameterName, Timestamp value) throws OdaException {}

    @Override
    public void setTimestamp(int parameterId, Timestamp value) throws OdaException {}

    @Override
    public void setBoolean(String parameterName, boolean value) throws OdaException {}

    @Override
    public void setBoolean(int parameterId, boolean value) throws OdaException {}

    @Override
    public void setNull(String parameterName) throws OdaException {}

    @Override
    public void setNull(int parameterId) throws OdaException {}

    @Override
    public int findInParameter(String parameterName) throws OdaException {
        return 0;
    }

    @Override
    public IParameterMetaData getParameterMetaData() throws OdaException {
        return new ParameterMetaData();
    }

    @Override
    public void setSortSpec(SortSpec sortBy) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortSpec getSortSpec() throws OdaException {
        return null;
    }

    @Override
    public void setObject(String arg0, Object arg1) throws OdaException {}

    @Override
    public void setObject(int arg0, Object arg1) throws OdaException {}

    @Override
    public void cancel() throws OdaException, UnsupportedOperationException {}

    @Override
    public String getEffectiveQueryText() {
        return null;
    }

    @Override
    public QuerySpecification getSpecification() {
        return null;
    }

    @Override
    public void setSpecification(QuerySpecification arg0) throws OdaException, UnsupportedOperationException {}
}
