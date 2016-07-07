package org.zenframework.z8.server.db.sql.expressions;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.sql.sql_string;

public class NotEqu extends Rel {
	public NotEqu(Field left, primary right) {
		this(new SqlField(left), new SqlConst(right));
	}

	public NotEqu(SqlToken left, primary right) {
		this(left, new SqlConst(right));
	}

	public NotEqu(Field left, String right) {
		this(new SqlField(left), right);
	}

	public NotEqu(SqlToken left, String right) {
		this(left, new sql_string(right));
	}

	public NotEqu(Field left, Field right) {
		this(new SqlField(left), new SqlField(right));
	}

	public NotEqu(SqlToken left, Field right) {
		this(left, new SqlField(right));
	}

	public NotEqu(Field left, SqlToken right) {
		this(new SqlField(left), right);
	}

	public NotEqu(SqlToken left, SqlToken right) {
		super(left, Operation.NotEq, right);
	}
}
