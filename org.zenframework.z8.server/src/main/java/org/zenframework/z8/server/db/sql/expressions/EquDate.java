package org.zenframework.z8.server.db.sql.expressions;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.date.TruncDay;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.sql.sql_date;

public class EquDate extends Rel {
	public EquDate(Field left, date right) {
		this(new SqlField(left), new SqlConst(right));
	}

	public EquDate(Field left, sql_date right) {
		this(new SqlField(left), right);
	}

	public EquDate(Field left, Field right) {
		this(new SqlField(left), new SqlField(right));
	}

	public EquDate(Field left, SqlToken right) {
		this(new SqlField(left), right);
	}

	public EquDate(SqlToken left, SqlToken right) {
		super(new TruncDay(left), Operation.Eq, new TruncDay(right));
	}
}
