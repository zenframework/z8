package org.zenframework.z8.server.db.sql.functions.vector;

import org.zenframework.z8.server.db.sql.SqlToken;

public class Cosine extends Scalar {

	public Cosine(SqlToken vector1, SqlToken vector2) {
		super(vector1, vector2, "<=>");
	}

}
