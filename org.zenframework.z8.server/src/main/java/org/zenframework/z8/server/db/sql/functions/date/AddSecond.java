package org.zenframework.z8.server.db.sql.functions.date;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.types.datespan;

public class AddSecond extends SqlToken {
	private SqlToken date;
	private SqlToken seconds;

	public AddSecond(SqlToken date, SqlToken seconds) {
		this.date = date;
		this.seconds = seconds;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		date.collectFields(fields);
		seconds.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		String dt = date.format(vendor, options);
		return "(" + dt + " + " + seconds.format(vendor, options) + " * " + datespan.TicksPerSecond + (vendor == DatabaseVendor.Postgres ? "::bigint" : "") + ")";
	}

	@Override
	public FieldType type() {
		return FieldType.Date;
	}
}
