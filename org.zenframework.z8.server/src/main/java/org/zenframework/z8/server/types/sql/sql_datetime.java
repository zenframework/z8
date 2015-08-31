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
import org.zenframework.z8.server.db.sql.functions.conversion.DateToChar;
import org.zenframework.z8.server.db.sql.functions.datetime.AddDay;
import org.zenframework.z8.server.db.sql.functions.datetime.AddHour;
import org.zenframework.z8.server.db.sql.functions.datetime.AddMinute;
import org.zenframework.z8.server.db.sql.functions.datetime.AddMonth;
import org.zenframework.z8.server.db.sql.functions.datetime.AddQuarter;
import org.zenframework.z8.server.db.sql.functions.datetime.AddSecond;
import org.zenframework.z8.server.db.sql.functions.datetime.AddYear;
import org.zenframework.z8.server.db.sql.functions.datetime.Day;
import org.zenframework.z8.server.db.sql.functions.datetime.Hour;
import org.zenframework.z8.server.db.sql.functions.datetime.Minute;
import org.zenframework.z8.server.db.sql.functions.datetime.Month;
import org.zenframework.z8.server.db.sql.functions.datetime.Quarter;
import org.zenframework.z8.server.db.sql.functions.datetime.Second;
import org.zenframework.z8.server.db.sql.functions.datetime.ServerTime;
import org.zenframework.z8.server.db.sql.functions.datetime.SetDate;
import org.zenframework.z8.server.db.sql.functions.datetime.TruncDay;
import org.zenframework.z8.server.db.sql.functions.datetime.TruncHour;
import org.zenframework.z8.server.db.sql.functions.datetime.TruncMinute;
import org.zenframework.z8.server.db.sql.functions.datetime.TruncMonth;
import org.zenframework.z8.server.db.sql.functions.datetime.TruncQuarter;
import org.zenframework.z8.server.db.sql.functions.datetime.TruncSecond;
import org.zenframework.z8.server.db.sql.functions.datetime.TruncYear;
import org.zenframework.z8.server.db.sql.functions.datetime.WeekDay;
import org.zenframework.z8.server.db.sql.functions.datetime.Year;
import org.zenframework.z8.server.db.sql.functions.datetime.YearDay;
import org.zenframework.z8.server.types.datetime;

public class sql_datetime extends sql_primary {
    public sql_datetime() {
        super(new SqlConst(new datetime()));
    }

    public sql_datetime(datetime value) {
        super(new SqlConst(value));
    }

    public sql_datetime(SqlToken token) {
        super(token);
    }

    static public sql_datetime z8_serverTime() {
        return new sql_datetime(new ServerTime());
    }

    @Override
    public sql_string z8_toString() {
        return new sql_string(new DateToChar(this));
    }

    public sql_date z8_toDate() {
        return z8_toString().z8_toDate();
    }

    public sql_integer z8_quarter() {
        return new sql_integer(new Quarter(this));
    }

    public sql_integer z8_year() {
        return new sql_integer(new Year(this));
    }

    public sql_integer z8_month() {
        return new sql_integer(new Month(this));
    }

    public sql_integer z8_day() {
        return new sql_integer(new Day(this));
    }

    public sql_integer z8_weekDay() {
        return new sql_integer(new WeekDay(this));
    }

    public sql_integer z8_yearDay() {
        return new sql_integer(new YearDay(this));
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

    public void z8_set(sql_integer year, sql_integer month, sql_integer day, sql_integer hour, sql_integer minute,
            sql_integer second) {
        setToken(new SetDate(year, month, day, hour, minute, second));
    }

    public sql_datetime z8_truncQuarter() {
        return new sql_datetime(new TruncQuarter(this));
    }

    public sql_datetime z8_truncYear() {
        return new sql_datetime(new TruncYear(this));
    }

    public sql_datetime z8_truncMonth() {
        return new sql_datetime(new TruncMonth(this));
    }

    public sql_datetime z8_truncDay() {
        return new sql_datetime(new TruncDay(this));
    }

    public sql_datetime z8_truncHour() {
        return new sql_datetime(new TruncHour(this));
    }

    public sql_datetime z8_truncMinute() {
        return new sql_datetime(new TruncMinute(this));
    }

    public sql_datetime z8_truncSecond() {
        return new sql_datetime(new TruncSecond(this));
    }

    public sql_datetime z8_addQuarter(sql_integer count) {
        return new sql_datetime(new AddQuarter(this, count));
    }

    public sql_datetime z8_addYear(sql_integer count) {
        return new sql_datetime(new AddYear(this, count));
    }

    public sql_datetime z8_addMonth(sql_integer count) {
        return new sql_datetime(new AddMonth(this, count));
    }

    public sql_datetime z8_addDay(sql_integer count) {
        return new sql_datetime(new AddDay(this, count));
    }

    public sql_datetime z8_addHour(sql_integer count) {
        return new sql_datetime(new AddHour(this, count));
    }

    public sql_datetime z8_addMinute(sql_integer count) {
        return new sql_datetime(new AddMinute(this, count));
    }

    public sql_datetime z8_addSecond(sql_integer count) {
        return new sql_datetime(new AddSecond(this, count));
    }

    public sql_datetime z8_max() {
        return new sql_datetime(new Max(this));
    }

    public sql_datetime z8_min() {
        return new sql_datetime(new Min(this));
    }

    public sql_integer z8_count() {
        return new sql_integer(new Count(this));
    }

    public sql_datetime operatorPriority() {
        return new sql_datetime(new Group(this));
    }

    public sql_datetime operatorAdd(sql_datespan value) {
        return new sql_datetime(new Add(this, Operation.Add, value));
    }

    public sql_datetime operatorSub(sql_datespan value) {
        return new sql_datetime(new Add(this, Operation.Sub, value));
    }

    public sql_datespan operatorSub(sql_date value) {
        return new sql_datespan(new Add(this, Operation.Sub, value));
    }

    public sql_datespan operatorSub(sql_datetime value) {
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
