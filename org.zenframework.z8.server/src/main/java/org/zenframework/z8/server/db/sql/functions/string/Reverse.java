package org.zenframework.z8.server.db.sql.functions.string;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;

public class Reverse extends SqlToken {
	private SqlToken string;

	public Reverse(SqlToken string) {
		this.string = string;
	}

	@Override
	public void collectFields(Collection<IValue> fields) {
		string.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		return "REVERSE(" + string.format(vendor, options) + ")";
	}

	@Override
	public FieldType type() {
		return string.type();
	}
}
