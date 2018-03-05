package org.zenframework.z8.server.db.sql;

import org.zenframework.z8.server.db.sql.expressions.Group;

public class Sql {
	static public Group group(SqlToken token) {
		return new Group(token);
	}
}
