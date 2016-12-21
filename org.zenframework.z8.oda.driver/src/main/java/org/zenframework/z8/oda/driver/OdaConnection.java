package org.zenframework.z8.oda.driver;

import java.util.Properties;

import org.eclipse.datatools.connectivity.oda.IConnection;
import org.eclipse.datatools.connectivity.oda.IDataSetMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.OdaException;

import com.ibm.icu.util.ULocale;

public class OdaConnection implements IConnection {
	private boolean isOpen = false;

	@Override
	public void open(Properties connectionProperties) throws OdaException {
		isOpen = true;
	}

	@Override
	public void setAppContext(Object context) throws OdaException {
	}

	@Override
	public void close() throws OdaException {
		isOpen = false;
	}

	@Override
	public boolean isOpen() throws OdaException {
		return isOpen;
	}

	@Override
	public IDataSetMetaData getMetaData(String dataSetType) throws OdaException {
		return new DataSetMetaData(this);
	}

	@Override
	public IQuery newQuery(String dataSetType) throws OdaException {
		return new OdaQuery();
	}

	@Override
	public int getMaxQueries() throws OdaException {
		return 0;
	}

	@Override
	public void commit() throws OdaException {
	}

	@Override
	public void rollback() throws OdaException {
	}

	@Override
	public void setLocale(ULocale arg0) throws OdaException {
	}
}
