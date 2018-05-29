package org.zenframework.z8.server.db.sql.functions.date;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
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
		String dt = date.format(vendor, options);
		return "(" + dt + " + " + hours.format(vendor, options) + " *" + datespan.TicksPerHour + (vendor == DatabaseVendor.Postgres ? "::bigint" : "") + ")";
	}

	@Override
	public FieldType type() {
		return FieldType.Date;
	}
}
