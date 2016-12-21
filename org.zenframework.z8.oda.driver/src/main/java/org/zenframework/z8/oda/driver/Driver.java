package org.zenframework.z8.oda.driver;

import org.eclipse.datatools.connectivity.oda.IConnection;
import org.eclipse.datatools.connectivity.oda.IDriver;
import org.eclipse.datatools.connectivity.oda.LogConfiguration;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.util.manifest.DataTypeMapping;
import org.eclipse.datatools.connectivity.oda.util.manifest.ExtensionManifest;
import org.eclipse.datatools.connectivity.oda.util.manifest.ManifestExplorer;

public class Driver implements IDriver {
	static String ODA_DATA_SOURCE_ID = "org.zenframework.z8.oda.driver";

	@Override
	public IConnection getConnection(String dataSourceType) throws OdaException {
		return new OdaConnection();
	}

	@Override
	public void setLogConfiguration(LogConfiguration logConfig) throws OdaException {
	}

	@Override
	public int getMaxConnections() throws OdaException {
		return 0;
	}

	@Override
	public void setAppContext(Object context) throws OdaException {
		return;
	}

	static ExtensionManifest getManifest() throws OdaException {
		return ManifestExplorer.getInstance().getExtensionManifest(ODA_DATA_SOURCE_ID);
	}

	static String getNativeDataTypeName(int nativeDataTypeCode) throws OdaException {
		DataTypeMapping typeMapping = getManifest().getDataSetType(null).getDataTypeMapping(nativeDataTypeCode);
		if(typeMapping != null)
			return typeMapping.getNativeType();
		return "Non-defined";
	}
}
