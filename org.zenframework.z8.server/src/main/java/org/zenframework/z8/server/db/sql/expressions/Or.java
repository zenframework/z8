package org.zenframework.z8.server.db.sql.expressions;

import java.util.Collection;
import java.util.Iterator;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.If;
import org.zenframework.z8.server.types.sql.sql_integer;

public class Or extends Expression {
	static public String sqlOr = "or";

	static public Or fromList(Collection<SqlToken> tokens) {
		if(tokens.size() < 2)
			throw new RuntimeException("Or.fromList: incorrect number of parameters");

		Iterator<SqlToken> iterator = tokens.iterator();
		Or or = new Or(iterator.next(), iterator.next());

		while(iterator.hasNext())
			or = new Or(or, iterator.next());

		return or;
	}

	public Or(SqlToken left, SqlToken right) {
		super(left, Operation.Or, right);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		String result = left.format(vendor, options, true) + " OR " + right.format(vendor, options, true);

		if(!logicalContext) {
			SqlToken token = new If(new SqlStringToken(result, FieldType.Boolean), sql_integer.One, sql_integer.Zero);
			return token.format(vendor, options, false);
		}

		return result;
	}

	@Override
	public FieldType type() {
		return FieldType.Boolean;
	}
}
