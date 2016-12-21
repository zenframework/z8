package org.zenframework.z8.server.db.sql.functions.date;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class AddYear extends SqlToken {
	private SqlToken date;
	private SqlToken years;

	public AddYear(SqlToken date, SqlToken years) {
		this.date = date;
		this.years = years;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		date.collectFields(fields);
		years.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(vendor) {
		case Oracle:
			return "ADD_MONTHS(" + date.format(vendor, options) + ",(" + years.format(vendor, options) + ")*12)";
		case Postgres:
			return "(" + date.format(vendor, options) + " + (" + years.format(vendor, options) + ") * interval '1 year')";
		case SqlServer:
			return "DATEADD(YEAR, " + years.format(vendor, options) + ", " + date.format(vendor, options) + ")";
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return date.type();
	}
}
