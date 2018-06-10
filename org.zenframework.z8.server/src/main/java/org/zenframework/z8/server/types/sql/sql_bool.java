package org.zenframework.z8.server.types.sql;

import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Group;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Or;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.expressions.Unary;
import org.zenframework.z8.server.db.sql.functions.Count;
import org.zenframework.z8.server.db.sql.functions.If;
import org.zenframework.z8.server.db.sql.functions.Max;
import org.zenframework.z8.server.db.sql.functions.Min;
import org.zenframework.z8.server.db.sql.functions.conversion.ToString;
import org.zenframework.z8.server.types.bool;

public class sql_bool extends sql_primary {
	static public sql_bool True = new sql_bool(true);
	static public sql_bool False = new sql_bool(false);

	public sql_bool() {
		super(new SqlConst(bool.False));
	}

	public sql_bool(bool value) {
		super(new SqlConst(value));
	}

	public sql_bool(boolean value) {
		super(new SqlConst(new bool(value)));
	}

	public sql_bool(SqlToken token) {
		super(token);
	}

	@Override
	public sql_string z8_toString() {
		return new sql_string(new ToString(this));
	}

	public sql_bool z8_IIF(sql_bool yes, sql_bool no) {
		return new sql_bool(new If(this, yes, no));
	}

	public sql_datespan z8_IIF(sql_datespan yes, sql_datespan no) {
		return new sql_datespan(new If(this, yes, no));
	}

	public sql_date z8_IIF(sql_date yes, sql_date no) {
		return new sql_date(new If(this, yes, no));
	}

	public sql_decimal z8_IIF(sql_decimal yes, sql_decimal no) {
		return new sql_decimal(new If(this, yes, no));
	}

	public sql_guid z8_IIF(sql_guid yes, sql_guid no) {
		return new sql_guid(new If(this, yes, no));
	}

	public sql_integer z8_IIF(sql_integer yes, sql_integer no) {
		return new sql_integer(new If(this, yes, no));
	}

	public sql_string z8_IIF(sql_string yes, sql_string no) {
		return new sql_string(new If(this, yes, no));
	}

	public sql_geometry z8_IIF(sql_geometry yes, sql_geometry no) {
		return new sql_geometry(new If(this, yes, no));
	}

	public sql_bool z8_max() {
		return new sql_bool(new Max(this));
	}

	public sql_bool z8_min() {
		return new sql_bool(new Min(this));
	}

	public sql_integer z8_count() {
		return new sql_integer(new Count(this));
	}

	public sql_bool operatorPriority() {
		return new sql_bool(new Group(this));
	}

	public sql_bool operatorAnd(sql_bool value) {
		return new sql_bool(new And(this, value));
	}

	public sql_bool operatorOr(sql_bool value) {
		return new sql_bool(new Or(this, value));
	}

	public sql_bool operatorNot() {
		return new sql_bool(new Unary(Operation.Not, this));
	}

	public sql_bool operatorEqu(sql_bool value) {
		return new sql_bool(new Rel(this, Operation.Eq, value));
	}

	public sql_bool operatorNotEqu(sql_bool value) {
		return new sql_bool(new Rel(this, Operation.NotEq, value));
	}
}
