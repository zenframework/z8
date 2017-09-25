package org.zenframework.z8.server.db.sql.functions.date;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.datespan;

public class AddDay extends SqlToken {
	private SqlToken date;
	private SqlToken days;

	public AddDay(SqlToken date, SqlToken days) {
		this.date = date;
		this.days = days;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		date.collectFields(fields);
		days.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(vendor) {
		case Oracle:
			String dt = date.format(vendor, options);
			return "(" + dt + " + " + days.format(vendor, options) + " * " + datespan.TicksPerDay + ")";
		case Postgres:
			return "(" + date.format(vendor, options) + " + (" + days.format(vendor, options) + ") * interval '1 day')";
		case SqlServer:
			return "DATEADD(dd, " + days.format(vendor, options) + ", " + date.format(vendor, options) + ")";
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Date;
	}
}
