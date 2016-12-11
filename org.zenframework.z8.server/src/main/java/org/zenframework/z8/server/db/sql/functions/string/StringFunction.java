package org.zenframework.z8.server.db.sql.functions.string;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlToken;

public abstract class StringFunction extends SqlToken {
	protected StringFunction() {
	}

	@Override
	public FieldType type() {
		return FieldType.String;
	}
}
