package org.zenframework.z8.server.db.sql.functions.numeric;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;

public class Floor extends SqlToken {
	private SqlToken param1;

	public Floor(SqlToken p1) {
		param1 = p1;
	}

	@Override
	public void collectFields(Collection<IValue> fields) {
		param1.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		return "FLOOR(" + param1.format(vendor, options) + ")";
	}

	@Override
	public FieldType type() {
		return FieldType.Integer;
	}
}
