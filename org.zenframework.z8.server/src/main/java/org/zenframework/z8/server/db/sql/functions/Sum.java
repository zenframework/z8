package org.zenframework.z8.server.db.sql.functions;

import org.zenframework.z8.server.db.sql.SqlToken;

public class Sum extends Window {
	public Sum(SqlToken token) {
		super(token, "sum");
	}
}
