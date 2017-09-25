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
import org.zenframework.z8.server.db.sql.functions.datespan.AddDay;
import org.zenframework.z8.server.db.sql.functions.datespan.AddHour;
import org.zenframework.z8.server.db.sql.functions.datespan.AddMinute;
import org.zenframework.z8.server.db.sql.functions.datespan.AddSecond;
import org.zenframework.z8.server.db.sql.functions.datespan.Days;
import org.zenframework.z8.server.db.sql.functions.datespan.Hours;
import org.zenframework.z8.server.db.sql.functions.datespan.Minutes;
import org.zenframework.z8.server.db.sql.functions.datespan.Seconds;
import org.zenframework.z8.server.db.sql.functions.datespan.TotalHours;
import org.zenframework.z8.server.db.sql.functions.datespan.TotalMinutes;
import org.zenframework.z8.server.db.sql.functions.datespan.TotalSeconds;
import org.zenframework.z8.server.db.sql.functions.datespan.TruncHour;
import org.zenframework.z8.server.db.sql.functions.datespan.TruncMinute;
import org.zenframework.z8.server.db.sql.functions.datespan.TruncSecond;
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

	public sql_integer z8_days() {
		return new sql_integer(new Days(this));
	}

	public sql_integer z8_hours() {
		return new sql_integer(new Hours(this));
	}

	public sql_integer z8_minutes() {
		return new sql_integer(new Minutes(this));
	}

	public sql_integer z8_seconds() {
		return new sql_integer(new Seconds(this));
	}

	public sql_integer z8_totalHours() {
		return new sql_integer(new TotalHours(this));
	}

	public sql_integer z8_totalMinutes() {
		return new sql_integer(new TotalMinutes(this));
	}

	public sql_integer z8_totalSeconds() {
		return new sql_integer(new TotalSeconds(this));
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

	public sql_datespan z8_addDay(sql_integer days) {
		return new sql_datespan(new AddDay(this, days));
	}

	public sql_datespan z8_addHour(sql_integer hours) {
		return new sql_datespan(new AddHour(this, hours));
	}

	public sql_datespan z8_addMinute(sql_integer minutes) {
		return new sql_datespan(new AddMinute(this, minutes));
	}

	public sql_datespan z8_addSecond(sql_integer seconds) {
		return new sql_datespan(new AddSecond(this, seconds));
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
