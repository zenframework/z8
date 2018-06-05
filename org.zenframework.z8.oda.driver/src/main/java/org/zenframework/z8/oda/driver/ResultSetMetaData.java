package org.zenframework.z8.oda.driver;

import java.util.List;

import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.zenframework.z8.server.base.table.value.DateExpression;
import org.zenframework.z8.server.base.table.value.DateField;
import org.zenframework.z8.server.base.table.value.IField;

public class ResultSetMetaData implements IResultSetMetaData {
	private List<IField> columns;
	private String context;

	public ResultSetMetaData(List<IField> columns, String context) {
		this.columns = columns;
		this.context = context;
	}

	@Override
	public int getColumnCount() throws OdaException {
		return columns.size();
	}

	public IField getColumn(int index) throws OdaException {
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

	static public int getNativeTypeCode(IField column) {
		switch(column.type()) {
		case Guid:
		case String:
		case Boolean:
			return 1;
		case Integer:
			return 4;
		case Date:
			return column instanceof DateField || column instanceof DateExpression ? 91 : 93;
		case Decimal:
			return 3;
		case Binary:
			return 2004;
		default:
			return 1;
		}
	}

	@Override
	public int getColumnType(int index) throws OdaException {
		IField column = getColumn(index);
		return getNativeTypeCode(column);
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
