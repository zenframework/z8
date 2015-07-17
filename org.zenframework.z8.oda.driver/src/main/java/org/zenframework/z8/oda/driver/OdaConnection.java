package org.zenframework.z8.oda.driver;

import java.util.Properties;

import org.eclipse.core.runtime.Path;
import org.eclipse.datatools.connectivity.oda.IConnection;
import org.eclipse.datatools.connectivity.oda.IDataSetMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.OdaException;

import org.zenframework.z8.oda.driver.connection.Connection;
import org.zenframework.z8.server.config.SystemProperty;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.security.IUser;

import com.ibm.icu.util.ULocale;

public class OdaConnection implements IConnection {
    private Connection m_connection = null;

    @Override
    public void open(Properties connectionProperties) throws OdaException {
        String url = getUrl();
        String login = null;
        String password = null;

        if(Connection.isInRuntimeMode()) {
            IUser user = ApplicationServer.getUser();
            login = user.name();
            password = user.password();
        }
        else {
            login = (String)connectionProperties.get(Constants.User);
            password = (String)connectionProperties.get(Constants.Password);
        }

        if(login == null)
            login = "";
        if(password == null)
            password = "";

        m_connection = Connection.connect(url, login, password);
    }

    public String getUrl() {
        String path = System.getProperty(SystemProperty.WebInfPath);

        if(path != null) {
            Path webInfPath = new Path(path);
            return webInfPath.removeLastSegments(1).toString();
        }
        return null;
    }

    @Override
    public void setAppContext(Object context) throws OdaException {}

    @Override
    public void close() throws OdaException {
        Connection.disconnect(m_connection);
    }

    @Override
    public boolean isOpen() throws OdaException {
        return m_connection != null;
    }

    @Override
    public IDataSetMetaData getMetaData(String dataSetType) throws OdaException {
        return new DataSetMetaData(this);
    }

    @Override
    public IQuery newQuery(String dataSetType) throws OdaException {
        return new OdaQuery(m_connection);
    }

    @Override
    public int getMaxQueries() throws OdaException {
        return 0;
    }

    @Override
    public void commit() throws OdaException {}

    @Override
    public void rollback() throws OdaException {}

    @Override
    public void setLocale(ULocale arg0) throws OdaException {}
}
