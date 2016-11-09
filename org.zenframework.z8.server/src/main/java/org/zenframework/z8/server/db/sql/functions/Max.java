package org.zenframework.z8.server.db.sql.functions;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.conversion.ToChar;

public class Max extends Window {
	public Max(SqlToken token) {
		super(token.type() == FieldType.Guid || token.type() == FieldType.Text ? new ToChar(token) : token, "max");
	}
}
