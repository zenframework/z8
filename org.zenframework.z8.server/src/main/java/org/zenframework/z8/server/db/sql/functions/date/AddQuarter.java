package org.zenframework.z8.server.db.sql.functions.date;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class AddQuarter extends SqlToken {
	private SqlToken date;
	private SqlToken quarters;

	public AddQuarter(SqlToken date, SqlToken quarters) {
		this.date = date;
		this.quarters = quarters;
	}

	@Override
	public void collectFields(Collection<IValue> fields) {
		date.collectFields(fields);
		quarters.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(vendor) {
		case Oracle:
			return "ADD_MONTHS(" + date.format(vendor, options) + ",(" + quarters.format(vendor, options) + ")*3)";
		case Postgres:
			return "(" + date.format(vendor, options) + " + (" + quarters.format(vendor, options) + ") * interval '3 months')";
		case SqlServer:
			return "DATEADD(qq, " + quarters.format(vendor, options) + ", " + date.format(vendor, options) + ")";
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return date.type();
	}
}
