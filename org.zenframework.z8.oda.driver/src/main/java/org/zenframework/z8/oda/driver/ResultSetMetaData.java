package org.zenframework.z8.oda.driver;

import java.util.List;

import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.zenframework.z8.server.base.table.value.Field;

public class ResultSetMetaData implements IResultSetMetaData {
	private List<Field> columns;
	private String context;

	public ResultSetMetaData(List<Field> columns, String context) {
		this.columns = columns;
		this.context = context;
	}

	@Override
	public int getColumnCount() throws OdaException {
		return columns.size();
	}

	public Field getColumn(int index) throws OdaException {
		return columns.get(index - 1);
	}

	@Override
	public String getColumnName(int index) throws OdaException {
		String id = getColumn(index).id();
		if(context != null && context != null && id.startsWith(context + "."))
			return id.substring(context.length() + 1);
		return id;
	}

	@Override
	public String getColumnLabel(int index) throws OdaException {
		return getColumn(index).displayName();
	}

	@Override
	public int getColumnType(int index) throws OdaException {
		Field column = getColumn(index);

		switch(column.type()) {
		case Guid:
		case String:
		case Boolean:
			return 1;
		case Integer:
			return 4;
		case Date:
			return 91;
		case Datetime:
			return 93;
		case Decimal:
			return 3;
		default:
			return 1;
		}
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
