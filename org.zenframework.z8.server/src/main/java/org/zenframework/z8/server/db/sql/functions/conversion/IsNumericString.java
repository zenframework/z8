package org.zenframework.z8.server.db.sql.functions.conversion;

import org.zenframework.z8.server.db.sql.SqlToken;

public class IsNumericString extends IsIntString {
	public IsNumericString(SqlToken numeric) {
		super(numeric);
	}

	@Override
	protected String pattern() {
		return "%[^0-9|.]%";
	}
}
