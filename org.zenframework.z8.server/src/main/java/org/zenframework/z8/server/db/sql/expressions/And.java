package org.zenframework.z8.server.db.sql.expressions;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.If;
import org.zenframework.z8.server.types.sql.sql_integer;

public class And extends Expression {
	static public String sqlAnd = "and";

	public And(SqlToken left, SqlToken right) {
		super(left, Operation.And, right);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		if(logicalContext) {
			SqlToken left = this.left instanceof True ? null : (this.left instanceof Group ? this.left : new Group(this.left));
			SqlToken right = this.right instanceof True ? null : (this.right instanceof Group ? this.right : new Group(this.right));

			String leftText = left == null ? null : left.format(vendor, options, true);
			String rightText = right == null ? null : right.format(vendor, options, true);

			if(leftText != null && rightText != null) {
				return leftText + " AND " + rightText;
			} else if(leftText != null) {
				return leftText;
			} else if(rightText != null) {
				return rightText;
			}
			return null;
		} else {
			SqlToken token = new If(this, new sql_integer(1), new sql_integer(0));
			return token.format(vendor, options);
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Boolean;
	}
}
