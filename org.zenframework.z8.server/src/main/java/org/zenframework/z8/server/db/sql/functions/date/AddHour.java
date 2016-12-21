package org.zenframework.z8.server.db.sql.functions.date;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.datespan;

public class AddHour extends SqlToken {
	private SqlToken date;
	private SqlToken hours;

	public AddHour(SqlToken date, SqlToken hours) {
		this.date = date;
		this.hours = hours;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		date.collectFields(fields);
		hours.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(date.type()) {
		case Date:
		case Datetime:
			switch(vendor) {
			case Oracle:
				return "(" + date.format(vendor, options) + "+(" + hours.format(vendor, options) + ")/24)";
			case Postgres:
				return "(" + date.format(vendor, options) + " + (" + hours.format(vendor, options) + ") * interval '1 hour')";
			case SqlServer:
				return "DATEADD(hh, " + hours.format(vendor, options) + ", " + date.format(vendor, options) + ")";
			default:
				throw new UnknownDatabaseException();
			}

		case Datespan:
			return date.format(vendor, options) + "+(" + hours.format(vendor, options) + "*" + datespan.TicksPerHour + ")";

		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public FieldType type() {
		return date.type();
	}
}
