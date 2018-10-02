package org.zenframework.z8.server.db.sql.functions.date;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;

public class ExtractDay extends ExtractDatePart {
	public ExtractDay(SqlToken date) {
		super(date, Day);
	}

	public ExtractDay(Field field) {
		this(new SqlField(field));
	}
}
