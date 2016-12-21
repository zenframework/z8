package org.zenframework.z8.oda.driver;

import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.FieldType;

public class ResultSetMetaData implements IResultSetMetaData {
	private IField[] m_columns = null;

	public ResultSetMetaData(IField[] columns) {
		m_columns = columns;
	}

	@Override
	public int getColumnCount() throws OdaException {
		return m_columns.length;
	}

	@Override
	public String getColumnName(int index) throws OdaException {
		return m_columns[index - 1].id();
	}

	@Override
	public String getColumnLabel(int index) throws OdaException {
		IField value = m_columns[index - 1];
		String displayName = value.displayName();
		return displayName != null ? displayName : value.id();
	}

	@Override
	public int getColumnType(int index) throws OdaException {
		if(m_columns[index - 1].type() == FieldType.Guid || m_columns[index - 1].type() == FieldType.String || m_columns[index - 1].type() == FieldType.Boolean)
			return 1;
		// else if (m_columns[index - 1].type() == FieldType.Boolean)
		// return 16;
		else if(m_columns[index - 1].type() == FieldType.Integer)
			return 4;
		else if(m_columns[index - 1].type() == FieldType.Date)
			return 91;
		else if(m_columns[index - 1].type() == FieldType.Datetime)
			return 93;
		else if(m_columns[index - 1].type() == FieldType.Decimal)
			return 3;

		return 1;
	}

	@Override
	public String getColumnTypeName(int index) throws OdaException {
		int nativeTypeCode = getColumnType(index);
		return Driver.getNativeDataTypeName(nativeTypeCode);
	}

	@Override
	public int getColumnDisplayLength(int index) throws OdaException {
		return 100;
	}

	@Override
	public int getPrecision(int index) throws OdaException {
		return -1;
	}

	@Override
	public int getScale(int index) throws OdaException {
		return -1;
	}

	@Override
	public int isNullable(int index) throws OdaException {
		return IResultSetMetaData.columnNullable;
	}
}
