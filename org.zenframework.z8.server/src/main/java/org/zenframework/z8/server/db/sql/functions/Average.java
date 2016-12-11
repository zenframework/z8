package org.zenframework.z8.server.db.sql.functions;

import org.zenframework.z8.server.db.sql.SqlToken;

public class Average extends Window {
	public Average(SqlToken token) {
		super(token, "avg");
	}
}
