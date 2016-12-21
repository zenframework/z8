package org.zenframework.z8.oda.driver;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

public class ParameterMetaData implements IParameterMetaData {
	@Override
	public int getParameterCount() throws OdaException {
		return 0;
	}

	@Override
	public int getParameterMode(int param) throws OdaException {
		return IParameterMetaData.parameterModeIn;
	}

	@Override
	public String getParameterName(int param) throws OdaException {
		return null;
	}

	@Override
	public int getParameterType(int param) throws OdaException {
		return java.sql.Types.CHAR;
	}

	@Override
	public String getParameterTypeName(int param) throws OdaException {
		int nativeTypeCode = getParameterType(param);
		return Driver.getNativeDataTypeName(nativeTypeCode);
	}

	@Override
	public int getPrecision(int param) throws OdaException {
		return -1;
	}

	@Override
	public int getScale(int param) throws OdaException {
		return -1;
	}

	@Override
	public int isNullable(int param) throws OdaException {
		return IParameterMetaData.parameterNullableUnknown;
	}

}
