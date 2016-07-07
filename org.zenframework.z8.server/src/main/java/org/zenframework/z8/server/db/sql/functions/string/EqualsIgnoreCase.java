package org.zenframework.z8.server.db.sql.functions.string;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_string;

public class EqualsIgnoreCase extends Rel {
	public EqualsIgnoreCase(Field left, string right) {
		this(new SqlField(left), right.get());
	}

	public EqualsIgnoreCase(Field left, String right) {
		this(new SqlField(left), right);
	}

	public EqualsIgnoreCase(SqlToken left, string right) {
		this(left, right.get());
	}

	public EqualsIgnoreCase(SqlToken left, String right) {
		this(left, new sql_string(right.toLowerCase()));
	}

	public EqualsIgnoreCase(Field left, Field right) {
		this(new SqlField(left), new SqlField(right));
	}

	public EqualsIgnoreCase(SqlToken left, Field right) {
		this(left, new SqlField(right));
	}

	public EqualsIgnoreCase(Field left, SqlToken right) {
		this(new SqlField(left), right);
	}

	public EqualsIgnoreCase(SqlToken left, SqlToken right) {
		super(new Lower(left), Operation.Eq, new Lower(right));
	}
}
