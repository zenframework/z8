package org.zenframework.z8.server.db.sql.functions;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlToken;

public class Sum extends Window {
	public Sum(SqlToken token) {
		super(token, getSumFunction(token));
	}

	protected static String getSumFunction(SqlToken token) {
		return token.type() == FieldType.Geometry ? "ST_Union" : "sum";
	}
}
