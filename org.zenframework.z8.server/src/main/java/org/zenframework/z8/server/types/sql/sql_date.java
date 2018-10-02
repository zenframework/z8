package org.zenframework.z8.server.types.sql;

import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Add;
import org.zenframework.z8.server.db.sql.expressions.Group;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.functions.Count;
import org.zenframework.z8.server.db.sql.functions.Max;
import org.zenframework.z8.server.db.sql.functions.Min;
import org.zenframework.z8.server.db.sql.functions.conversion.DateToInt;
import org.zenframework.z8.server.db.sql.functions.conversion.DateToString;
import org.zenframework.z8.server.db.sql.functions.date.AddDay;
import org.zenframework.z8.server.db.sql.functions.date.AddHour;
import org.zenframework.z8.server.db.sql.functions.date.AddMinute;
import org.zenframework.z8.server.db.sql.functions.date.AddSecond;
import org.zenframework.z8.server.db.sql.functions.date.TruncDay;
import org.zenframework.z8.server.db.sql.functions.date.TruncHour;
import org.zenframework.z8.server.db.sql.functions.date.TruncMinute;
import org.zenframework.z8.server.db.sql.functions.date.TruncSecond;
import org.zenframework.z8.server.types.date;

public class sql_date extends sql_primary {
	public sql_date() {
		super(new SqlConst(new date()));
	}

	public sql_date(date value) {
		super(new SqlConst(value));
	}

	public sql_date(SqlToken token) {
		super(token);
	}

	@Override
	public sql_string z8_toString() {
		return new sql_string(new DateToString(this));
	}

	public sql_integer z8_toInt() {
		return new sql_integer(new DateToInt(this));
	}

	public sql_date z8_truncDay() {
		return new sql_date(new TruncDay(this));
	}

	public sql_date z8_truncHour() {
		return new sql_date(new TruncHour(this));
	}

	public sql_date z8_truncMinute() {
		return new sql_date(new TruncMinute(this));
	}

	public sql_date z8_truncSecond() {
		return new sql_date(new TruncSecond(this));
	}

	public sql_date z8_addDay(sql_integer days) {
		return new sql_date(new AddDay(this, days));
	}

	public sql_date z8_addHour(sql_integer hours) {
		return new sql_date(new AddHour(this, hours));
	}

	public sql_date z8_addMinute(sql_integer minutes) {
		return new sql_date(new AddMinute(this, minutes));
	}

	public sql_date z8_addSecond(sql_integer seconds) {
		return new sql_date(new AddSecond(this, seconds));
	}

	public sql_date z8_max() {
		return new sql_date(new Max(this));
	}

	public sql_date z8_min() {
		return new sql_date(new Min(this));
	}

	public sql_integer z8_count() {
		return new sql_integer(new Count(this));
	}

	public sql_date operatorPriority() {
		return new sql_date(new Group(this));
	}

	public sql_date operatorAdd(sql_datespan value) {
		return new sql_date(new Add(this, Operation.Add, value));
	}

	public sql_date operatorSub(sql_datespan value) {
		return new sql_date(new Add(this, Operation.Sub, value));
	}

	public sql_datespan operatorSub(sql_date value) {
		return new sql_datespan(new Add(this, Operation.Sub, value));
	}

	public sql_bool operatorLess(SqlToken value) {
		return new sql_bool(new Rel(this, Operation.LT, value));
	}

	public sql_bool operatorMore(SqlToken value) {
		return new sql_bool(new Rel(this, Operation.GT, value));
	}

	public sql_bool operatorLessEqu(SqlToken value) {
		return new sql_bool(new Rel(this, Operation.LE, value));
	}

	public sql_bool operatorMoreEqu(SqlToken value) {
		return new sql_bool(new Rel(this, Operation.GE, value));
	}

	public sql_bool operatorEqu(SqlToken value) {
		return new sql_bool(new Rel(this, Operation.Eq, value));
	}

	public sql_bool operatorNotEqu(SqlToken value) {
		return new sql_bool(new Rel(this, Operation.NotEq, value));
	}
}
