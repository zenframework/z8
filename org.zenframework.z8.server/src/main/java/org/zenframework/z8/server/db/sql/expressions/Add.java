package org.zenframework.z8.server.db.sql.expressions;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.string.Concat;

public class Add extends Operator {
	public Add(SqlToken left, Operation operation, SqlToken right) {
		super(left, operation, right);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(operation) {
		case Add:
			return left.type() == FieldType.String ? new Concat(left, right).format(vendor, options) : left + "+" + right;
		case Sub:
			return left.format(vendor, options) + "-" + right.format(vendor, options);
		default:
			throw new UnsupportedOperationException();
		}
	}
}
