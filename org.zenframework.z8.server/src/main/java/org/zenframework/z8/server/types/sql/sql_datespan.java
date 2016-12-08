package org.zenframework.z8.server.types.sql;

import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Add;
import org.zenframework.z8.server.db.sql.expressions.Group;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.functions.Average;
import org.zenframework.z8.server.db.sql.functions.Count;
import org.zenframework.z8.server.db.sql.functions.Max;
import org.zenframework.z8.server.db.sql.functions.Min;
import org.zenframework.z8.server.db.sql.functions.Sum;
import org.zenframework.z8.server.db.sql.functions.conversion.DatespanToString;
import org.zenframework.z8.server.db.sql.functions.date.AddDay;
import org.zenframework.z8.server.db.sql.functions.date.AddHour;
import org.zenframework.z8.server.db.sql.functions.date.AddMinute;
import org.zenframework.z8.server.db.sql.functions.date.AddSecond;
import org.zenframework.z8.server.db.sql.functions.date.Day;
import org.zenframework.z8.server.db.sql.functions.date.Hour;
import org.zenframework.z8.server.db.sql.functions.date.Minute;
import org.zenframework.z8.server.db.sql.functions.date.Second;
import org.zenframework.z8.server.db.sql.functions.date.TotalHour;
import org.zenframework.z8.server.db.sql.functions.date.TotalMinute;
import org.zenframework.z8.server.db.sql.functions.date.TotalSecond;
import org.zenframework.z8.server.db.sql.functions.date.TruncHour;
import org.zenframework.z8.server.db.sql.functions.date.TruncMinute;
import org.zenframework.z8.server.db.sql.functions.date.TruncSecond;
import org.zenframework.z8.server.types.datespan;

public class sql_datespan extends sql_primary {
	public sql_datespan() {
		super(new SqlConst(new datespan()));
	}

	public sql_datespan(datespan value) {
		super(new SqlConst(value));
	}

	public sql_datespan(SqlToken token) {
		super(token);
	}

	@Override
	public sql_string z8_toString() {
		return new sql_string(new DatespanToString(this));
	}

	public sql_integer z8_day() {
		return new sql_integer(new Day(this));
	}

	public sql_integer z8_hour() {
		return new sql_integer(new Hour(this));
	}

	public sql_integer z8_minute() {
		return new sql_integer(new Minute(this));
	}

	public sql_integer z8_second() {
		return new sql_integer(new Second(this));
	}

	public sql_integer z8_totalHours() {
		return new sql_integer(new TotalHour(this));
	}

	public sql_integer z8_totalMinutes() {
		return new sql_integer(new TotalMinute(this));
	}

	public sql_integer z8_totalSeconds() {
		return new sql_integer(new TotalSecond(this));
	}

	public sql_datespan z8_truncHour() {
		return new sql_datespan(new TruncHour(this));
	}

	public sql_datespan z8_truncMinute() {
		return new sql_datespan(new TruncMinute(this));
	}

	public sql_datespan z8_truncSecond() {
		return new sql_datespan(new TruncSecond(this));
	}

	public sql_datespan z8_addDay(sql_integer count) {
		return new sql_datespan(new AddDay(this, count));
	}

	public sql_datespan z8_addHour(sql_integer count) {
		return new sql_datespan(new AddHour(this, count));
	}

	public sql_datespan z8_addMinute(sql_integer count) {
		return new sql_datespan(new AddMinute(this, count));
	}

	public sql_datespan z8_addSecond(sql_integer count) {
		return new sql_datespan(new AddSecond(this, count));
	}

	public sql_datespan z8_max() {
		return new sql_datespan(new Max(this));
	}

	public sql_datespan z8_min() {
		return new sql_datespan(new Min(this));
	}

	public sql_integer z8_count() {
		return new sql_integer(new Count(this));
	}

	public sql_datespan z8_average() {
		return new sql_datespan(new Average(this));
	}

	public sql_datespan z8_sum() {
		return new sql_datespan(new Sum(this));
	}

	public sql_datespan operatorPriority() {
		return new sql_datespan(new Group(this));
	}

	public sql_date operatorAdd(sql_date value) {
		return new sql_date(new Add(this, Operation.Add, value));
	}

	public sql_datespan operatorAdd(sql_datespan value) {
		return new sql_datespan(new Add(this, Operation.Add, value));
	}

	public sql_datespan operatorSub(sql_datespan value) {
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
