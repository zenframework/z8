package org.zenframework.z8.server.db.sql.functions.date;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;

public class ExtractYear extends ExtractDatePart {
	public ExtractYear(SqlToken date) {
		super(date, Year);
	}

	public ExtractYear(Field field) {
		this(new SqlField(field));
	}
}
