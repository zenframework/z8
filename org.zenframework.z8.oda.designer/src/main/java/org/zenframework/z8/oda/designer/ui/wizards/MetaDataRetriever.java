package org.zenframework.z8.oda.designer.ui.wizards;

import java.util.Properties;

import org.eclipse.datatools.connectivity.oda.IAdvancedQuery;
import org.eclipse.datatools.connectivity.oda.IConnection;
import org.eclipse.datatools.connectivity.oda.IDriver;
import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.design.DataSetDesign;
import org.eclipse.datatools.connectivity.oda.design.DataSourceDesign;
import org.eclipse.datatools.connectivity.oda.design.ui.designsession.DesignSessionUtil;

import org.zenframework.z8.oda.driver.Driver;

class MetaDataRetriever {
    private IResultSetMetaData resultMeta;
    private IParameterMetaData paramMeta;
    private IQuery query;
    private IConnection connection;

    MetaDataRetriever(DataSetDesign dataSetDesign) {
        DataSourceDesign dataSourceDesign = dataSetDesign.getDataSourceDesign();

        IDriver driver = new Driver();

        try {
            connection = driver.getConnection(dataSourceDesign.getOdaExtensionId());
            Properties prop = DesignSessionUtil.getEffectiveDataSourceProperties(dataSourceDesign);
            connection.open(prop);

            query = connection.newQuery(dataSetDesign.getOdaExtensionDataSetId());
            query.prepare(dataSetDesign.getQueryText());

            paramMeta = query.getParameterMetaData();

            if(query instanceof IAdvancedQuery) {
                resultMeta = query.getMetaData();
            }

        }
        catch(OdaException e) {}

    }

    IParameterMetaData getParameterMetaData() {
        return this.paramMeta;
    }

    IResultSetMetaData getResultSetMetaData() {
        return this.resultMeta;
    }

    void close() {
        try {
            if(query != null) {
                query.close();
            }
            if(connection != null) {
                connection.close();
            }
        }
        catch(OdaException e) {}
    }
}
