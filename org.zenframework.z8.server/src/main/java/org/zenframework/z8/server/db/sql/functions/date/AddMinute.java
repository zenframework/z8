package org.zenframework.z8.server.db.sql.functions.date;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.types.datespan;

public class AddMinute extends SqlToken {
	private SqlToken date;
	private SqlToken minutes;

	public AddMinute(SqlToken date, SqlToken minutes) {
		this.date = date;
		this.minutes = minutes;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		date.collectFields(fields);
		minutes.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		String dt = date.format(vendor, options);
		return "(" + dt + " + " + minutes.format(vendor, options) + " *" + datespan.TicksPerMinute + ")";
	}

	@Override
	public FieldType type() {
		return FieldType.Date;
	}
}
