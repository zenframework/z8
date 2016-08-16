package org.zenframework.z8.server.db.sql.expressions;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.types.sql.sql_bool;

public class Group extends Expression {
	public Group(SqlToken l) {
		super(l, Operation.None, null);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		String string = this.left.format(vendor, options, logicalContext);

		if(string != null) {
			SqlToken token = left;

			if(left instanceof sql_bool) {
				token = ((sql_bool)left).getToken();
			}

			if(token instanceof And || token instanceof Rel || token instanceof SqlConst || token instanceof Group) {
				return string;
			}

			return "(" + string + ")";
		}
		return null;
	}

	@Override
	public FieldType type() {
		return left.type();
	}
}
