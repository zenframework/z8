package org.zenframework.z8.server.db.sql.functions.datespan;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.types.datespan;

public class AddHour extends SqlToken {
	private SqlToken span;
	private SqlToken hours;

	public AddHour(SqlToken span, SqlToken hours) {
		this.span = span;
		this.hours = hours;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		span.collectFields(fields);
		hours.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		return span.format(vendor, options) + " + (" + hours.format(vendor, options) + " * " + datespan.TicksPerHour + ")";
	}

	@Override
	public FieldType type() {
		return FieldType.Datespan;
	}
}
