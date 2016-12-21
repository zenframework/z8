package org.zenframework.z8.server.db.sql.functions.numeric;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;

public class Sign extends SqlToken {
	private SqlToken value;

	public Sign(SqlToken value) {
		this.value = value;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		value.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		return "SIGN(" + value.format(vendor, options) + ")";
	}

	@Override
	public FieldType type() {
		return FieldType.Integer;
	}
}
