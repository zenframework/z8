package org.zenframework.z8.server.db.sql.functions.date;

import java.util.Collection;
import java.util.TimeZone;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class TruncDay extends SqlToken {
	private SqlToken date;

	public TruncDay(Field field) {
		this(new SqlField(field));
	}

	public TruncDay(SqlToken date) {
		this.date = date;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		date.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(vendor) {
		case Oracle:
			String dt = date.format(vendor, options);
			String offset = "(" + TimeZone.getDefault().getRawOffset() + ")";
			//return "(" + dt + " - MOD(" + dt + ", 86400000))";
			return "(" + dt + " + " + offset + " - MOD(" + dt + " + " + offset + ", 86400000)) - " + offset;
			//return TRUNC(" + date.format(vendor, options) + ", 'DD')";
		case Postgres:
			return "date_trunc('day', " + date.format(vendor, options) + ")";
		case SqlServer:
			return "CONVERT(datetime, CONVERT(varchar(10)," + date.format(vendor, options) + ", 120), 120)";
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return date.type();
	}
}
