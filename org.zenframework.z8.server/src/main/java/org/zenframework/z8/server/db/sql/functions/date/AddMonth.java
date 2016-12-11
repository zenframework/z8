package org.zenframework.z8.server.db.sql.functions.date;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class AddMonth extends SqlToken {
	private SqlToken date;
	private SqlToken months;

	public AddMonth(SqlToken date, SqlToken months) {
		this.date = date;
		this.months = months;
	}

	@Override
	public void collectFields(Collection<IValue> fields) {
		date.collectFields(fields);
		months.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(vendor) {
		case Oracle:
			return "ADD_MONTHS(" + date.format(vendor, options) + "," + months.format(vendor, options) + ")";
		case Postgres:
			return "(" + date.format(vendor, options) + " + (" + months.format(vendor, options) + ") * interval '1 month')";
		case SqlServer:
			return "DATEADD(mm, " + months.format(vendor, options) + ", " + date.format(vendor, options) + ")";
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return date.type();
	}
}
