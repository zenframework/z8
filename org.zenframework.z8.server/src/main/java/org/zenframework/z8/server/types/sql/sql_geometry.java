package org.zenframework.z8.server.types.sql;

import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Group;
import org.zenframework.z8.server.db.sql.expressions.Intersects;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.functions.conversion.ToString;
import org.zenframework.z8.server.types.geometry;

public class sql_geometry extends sql_primary {
	public sql_geometry() {
		super(new SqlConst(new geometry()));
	}

	public sql_geometry(String value) {
		super(new SqlConst(new geometry(value)));
	}

	public sql_geometry(geometry value) {
		super(new SqlConst(value));
	}

	public sql_geometry(SqlToken token) {
		super(token);
	}

	@Override
	public sql_string z8_toString() {
		return new sql_string(new ToString(this));
	}

	public sql_geometry operatorPriority() {
		return new sql_geometry(new Group(this));
	}

	public sql_bool operatorEqu(sql_geometry value) {
		return new sql_bool(new Rel(this, Operation.Eq, value));
	}

	public sql_bool operatorNotEqu(sql_geometry value) {
		return new sql_bool(new Rel(this, Operation.NotEq, value));
	}

	public sql_bool operatorAnd(sql_geometry value) {
		return new sql_bool(new Intersects(this, value));
	}
}
