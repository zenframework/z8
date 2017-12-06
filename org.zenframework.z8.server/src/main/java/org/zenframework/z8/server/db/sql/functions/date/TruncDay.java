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
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datespan;

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

	private date getConst() {
		return date.isConst() && date.isDate() ? date.date() : null;
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		date date = getConst();

		if(date!= null) {
			date.set(date.truncDay());
			return this.date.format(vendor, options);
		}

		String field = this.date.format(vendor, options);
		return "(" + field + " - MOD(" + field + " + (" + TimeZone.getDefault().getRawOffset() + "), " + datespan.TicksPerDay + "))";
	}

	@Override
	public FieldType type() {
		return FieldType.Date;
	}
}
