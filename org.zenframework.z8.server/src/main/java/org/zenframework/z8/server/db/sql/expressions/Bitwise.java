package org.zenframework.z8.server.db.sql.expressions;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;

public class Bitwise extends Operator {
	public Bitwise(SqlToken left, Operation operation, SqlToken right) {
		super(left, operation, right);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(operation) {
		case BitwiseAnd:
			return "(" + left.format(vendor, options) + ") & (" + right.format(vendor, options) + ")";
		case BitwiseOr:
			return left.format(vendor, options) + " | " + right.format(vendor, options);
		case Xor:
			return "(" + left.format(vendor, options) + ") # (" + right.format(vendor, options) + ")";
		default:
			throw new UnsupportedOperationException();
		}
	}
}
