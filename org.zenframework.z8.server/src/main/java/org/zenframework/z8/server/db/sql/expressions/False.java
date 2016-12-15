package org.zenframework.z8.server.db.sql.expressions;

import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.sql.sql_bool;

public class False extends sql_bool {
	public False() {
		super(new bool(false));
	}
}
