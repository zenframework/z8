/*
package org.zenframework.z8.server.db.sql.expressions;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.date.TruncDay;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.sql.sql_date;

public class RelDate extends Rel {
	public RelDate(Field left, Operation operation, date right) {
		this(new SqlField(left), operation, new SqlConst(right));
	}

	public RelDate(Field left, Operation operation, sql_date right) {
		this(new SqlField(left), operation, right);
	}

	public RelDate(Field left, Operation operation, Field right) {
		this(new SqlField(left), operation, new SqlField(right));
	}

	public RelDate(Field left, Operation operation, SqlToken right) {
		this(new SqlField(left), operation, right);
	}

	public RelDate(SqlToken left, Operation operation, SqlToken right) {
		super(left, operation, new TruncDay(right));
	}
}

*/