package org.zenframework.z8.server.db.sql.functions.date;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;

public class ExtractMonth extends ExtractDatePart {
	public ExtractMonth(SqlToken date) {
		super(date, Month);
	}

	public ExtractMonth(Field field) {
		this(new SqlField(field));
	}
}
