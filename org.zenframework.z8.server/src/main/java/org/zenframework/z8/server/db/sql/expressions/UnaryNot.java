package org.zenframework.z8.server.db.sql.expressions;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.SqlToken;

public class UnaryNot extends Unary {
	public UnaryNot(Field field) {
		super(Operation.Not, field);
	}

	public UnaryNot(SqlToken token) {
		super(Operation.Not, token);
	}
}
