package org.zenframework.z8.server.db.sql.functions.date;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;

public class TruncSecond extends SqlToken {
	private SqlToken time;

	public TruncSecond(SqlToken time) {
		this.time = time;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		time.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		String dt = time.format(vendor, options);
		return "(" + dt + " - MOD(" + dt + ", 1000))";
	}

	@Override
	public FieldType type() {
		return FieldType.Date;
	}
}
