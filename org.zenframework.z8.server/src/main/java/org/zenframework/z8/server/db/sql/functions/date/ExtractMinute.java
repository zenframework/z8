package org.zenframework.z8.server.db.sql.functions.date;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;

public class ExtractMinute extends ExtractDatePart {
	public ExtractMinute(SqlToken date) {
		super(date, Minute);
	}

	public ExtractMinute(Field field) {
		this(new SqlField(field));
	}
}
