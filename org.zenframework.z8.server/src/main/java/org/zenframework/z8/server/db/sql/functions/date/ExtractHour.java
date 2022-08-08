package org.zenframework.z8.server.db.sql.functions.date;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;

public class ExtractHour extends ExtractDatePart {
	public ExtractHour(SqlToken date) {
		super(date, Hour);
	}

	public ExtractHour(Field field) {
		this(new SqlField(field));
	}
}
