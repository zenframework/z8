package org.zenframework.z8.server.db.sql.functions.date;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
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
		String dt = date.format(vendor, options);
		return "(" + dt + " + " + days.format(vendor, options) + " * " + datespan.TicksPerDay + ")";
	}

	@Override
	public FieldType type() {
		return FieldType.Date;
	}
}
