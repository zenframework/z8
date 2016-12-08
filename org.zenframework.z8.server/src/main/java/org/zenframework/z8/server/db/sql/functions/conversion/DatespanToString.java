package org.zenframework.z8.server.db.sql.functions.conversion;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;

public class DatespanToString extends SqlToken {
	private SqlToken datespan;

	public DatespanToString(SqlToken datespan) {
		this.datespan = datespan;
	}

	@Override
	public void collectFields(Collection<IValue> fields) {
		datespan.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		return datespan.format(vendor, options);
	}

	@Override
	public FieldType type() {
		return FieldType.String;
	}
}
