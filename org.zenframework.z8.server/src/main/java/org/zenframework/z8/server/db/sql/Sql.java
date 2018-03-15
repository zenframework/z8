package org.zenframework.z8.server.db.sql;

import org.zenframework.z8.server.db.sql.expressions.Group;
import org.zenframework.z8.server.db.sql.expressions.UnaryNot;

public class Sql {
	static public Group group(SqlToken token) {
		return new Group(token);
	}

	static public UnaryNot not(SqlToken token) {
		return new UnaryNot(token);
	}
}
