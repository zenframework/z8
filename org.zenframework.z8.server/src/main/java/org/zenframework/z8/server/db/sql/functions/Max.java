package org.zenframework.z8.server.db.sql.functions;

import org.zenframework.z8.server.db.sql.SqlToken;

public class Max extends Window {
	public Max(SqlToken token) {
		super(token, "max");
	}
}
