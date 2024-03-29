package org.zenframework.z8.server.db.sql.expressions;

import java.util.Collection;
import java.util.Iterator;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.If;
import org.zenframework.z8.server.types.sql.sql_bool;
import org.zenframework.z8.server.types.sql.sql_integer;

public class And extends Expression {
	static public String sqlAnd = "and";

	static public And fromList(Collection<SqlToken> tokens) {
		if(tokens.size() < 2)
			throw new RuntimeException("And.fromList: incorrect number of parameters");

		Iterator<SqlToken> iterator = tokens.iterator();
		And and = new And(iterator.next(), iterator.next());

		while(iterator.hasNext())
			and = new And(and, iterator.next());

		return and;
	}

	public And(SqlToken left, SqlToken right) {
		super(left, Operation.And, right);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		if(logicalContext) {
			SqlToken left = this.left == sql_bool.True ? null : (this.left instanceof Group ? this.left : new Group(this.left));
			SqlToken right = this.right == sql_bool.True ? null : (this.right instanceof Group ? this.right : new Group(this.right));

			String leftText = left == null ? null : left.format(vendor, options, true);
			String rightText = right == null ? null : right.format(vendor, options, true);

			if(leftText != null && rightText != null)
				return leftText + " AND " + rightText;
			else if(leftText != null)
				return leftText;
			else if(rightText != null)
				return rightText;

			return null;
		} else {
			SqlToken token = new If(this, sql_integer.One, sql_integer.Zero);
			return token.format(vendor, options);
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Boolean;
	}
}
