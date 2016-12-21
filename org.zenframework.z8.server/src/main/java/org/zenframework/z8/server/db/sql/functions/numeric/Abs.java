package org.zenframework.z8.server.db.sql.functions.numeric;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;

public class Abs extends SqlToken {
	private SqlToken token;

	public Abs(SqlToken token) {
		this.token = token;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		token.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		return "ABS(" + token.format(vendor, options) + ")";
	}

	@Override
	public FieldType type() {
		return token.type();
	}
}
