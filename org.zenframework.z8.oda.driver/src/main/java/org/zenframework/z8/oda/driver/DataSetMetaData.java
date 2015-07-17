package org.zenframework.z8.oda.driver;

import org.eclipse.datatools.connectivity.oda.IConnection;
import org.eclipse.datatools.connectivity.oda.IDataSetMetaData;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.OdaException;

public class DataSetMetaData implements IDataSetMetaData {
    private IConnection m_connection;

    DataSetMetaData(IConnection connection) {
        m_connection = connection;
    }

    @Override
    public IConnection getConnection() throws OdaException {
        return m_connection;
    }

    @Override
    public IResultSet getDataSourceObjects(String catalog, String schema, String object, String version) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getDataSourceMajorVersion() throws OdaException {
        return 1;
    }

    @Override
    public int getDataSourceMinorVersion() throws OdaException {
        return 0;
    }

    @Override
    public String getDataSourceProductName() throws OdaException {
        return "Z8 Data Source";
    }

    @Override
    public String getDataSourceProductVersion() throws OdaException {
        return Integer.toString(getDataSourceMajorVersion()) + "." + Integer.toString(getDataSourceMinorVersion());
    }

    @Override
    public int getSQLStateType() throws OdaException {
        return IDataSetMetaData.sqlStateSQL99;
    }

    @Override
    public boolean supportsMultipleResultSets() throws OdaException {
        return false;
    }

    @Override
    public boolean supportsMultipleOpenResults() throws OdaException {
        return false;
    }

    @Override
    public boolean supportsNamedResultSets() throws OdaException {
        return false;
    }

    @Override
    public boolean supportsNamedParameters() throws OdaException {
        return false;
    }

    @Override
    public boolean supportsInParameters() throws OdaException {
        return false;
    }

    @Override
    public boolean supportsOutParameters() throws OdaException {
        return false;
    }

    @Override
    public int getSortMode() {
        return IDataSetMetaData.sortModeNone;
    }
}
