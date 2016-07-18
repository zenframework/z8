package org.zenframework.z8.server.db.sql.expressions;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.sql.sql_string;

public class Equ extends Rel {
	public Equ(Field left, primary right) {
		this(new SqlField(left), new SqlConst(right));
	}

	public Equ(SqlToken left, primary right) {
		this(left, new SqlConst(right));
	}

	public Equ(Field left, String right) {
		this(new SqlField(left), right);
	}

	public Equ(SqlToken left, String right) {
		this(left, new sql_string(right));
	}

	public Equ(Field left, Field right) {
		this(new SqlField(left), new SqlField(right));
	}

	public Equ(SqlToken left, Field right) {
		this(left, new SqlField(right));
	}

	public Equ(Field left, SqlToken right) {
		this(new SqlField(left), right);
	}

	public Equ(SqlToken left, SqlToken right) {
		super(left, Operation.Eq, right);
	}
}
