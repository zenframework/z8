package org.zenframework.z8.server.db.sql.functions.conversion;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class ToDatetime extends SqlToken {
	private SqlToken param1;

	public ToDatetime(SqlToken p1) {
		param1 = p1;
	}

	@Override
	public void collectFields(Collection<IValue> fields) {
		param1.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(vendor) {
		case Oracle:
		case Postgres:
			return param1.format(vendor, options) + "::timestamp with time zone";
		case SqlServer:
			return "CONVERT([datetime]," + param1.format(vendor, options) + ",(103))";
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Datetime;
	}
}
