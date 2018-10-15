package org.zenframework.z8.server.db.sql.functions.conversion;

import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.string.RegLike;
import org.zenframework.z8.server.types.string;

public class IsNumericString extends RegLike {

	private static final String PATTERN = "^[0-9\\.]+$";

	public IsNumericString(SqlToken string) {
		super(string, new SqlConst(new string(PATTERN)));
	}

}