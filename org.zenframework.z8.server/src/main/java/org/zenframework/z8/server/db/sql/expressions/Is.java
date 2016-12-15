package org.zenframework.z8.server.db.sql.expressions;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.types.sql.sql_bool;

public class Is extends Rel {
	public Is(Field field) {
		this(new SqlField(field));
	}

	public Is(SqlToken token) {
		super(token, Operation.Eq, new sql_bool(true));
	}
}
