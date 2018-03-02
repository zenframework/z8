package org.zenframework.z8.server.db.sql;

import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.db.sql.expressions.Group;
import org.zenframework.z8.server.db.sql.functions.Array;
import org.zenframework.z8.server.db.sql.functions.Average;
import org.zenframework.z8.server.db.sql.functions.Concat;
import org.zenframework.z8.server.db.sql.functions.Count;
import org.zenframework.z8.server.db.sql.functions.Max;
import org.zenframework.z8.server.db.sql.functions.Min;
import org.zenframework.z8.server.db.sql.functions.Sum;

public class Sql {
	static public Group group(SqlToken token) {
		return new Group(token);
	}

	static public SqlToken aggregate(SqlToken token, Aggregation aggregation) {
		switch(aggregation) {
		case Sum:
			return new Sum(token);
		case Max:
			return new Max(token);
		case Min:
			return new Min(token);
		case Average:
			return new Average(token);
		case Count:
			return new Count(token);
		case Array:
			return new Array(token);
		case Concat:
			return new Concat(token);
		default:
			return token;
		}
	}
}
