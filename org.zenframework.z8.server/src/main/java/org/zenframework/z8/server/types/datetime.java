package org.zenframework.z8.server.types;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.functions.conversion.ToDatetime;
import org.zenframework.z8.server.types.sql.sql_datetime;

public class datetime extends primary {

	private static final long serialVersionUID = -5362639596768531077L;

	private static long UTC_0001_01_01 = -62135769600000l;
	private static long UTC_5000_01_01 = 95617584000000l;

	final static public datetime MIN = new datetime(UTC_0001_01_01);
	final static public datetime MAX = new datetime(UTC_5000_01_01);

	protected GregorianCalendar value = new GregorianCalendar();

	public datetime() {
	}

	public datetime(int year, int month, int day) {
		set(year, month, day, 0, 0, 0);
	}

	public datetime(int year, int month, int day, int hour, int minute, int second) {
		set(year, month, day, hour, minute, second);
	}

	public datetime(long milliseconds) {
		set(milliseconds);
	}

	public datetime(GregorianCalendar gc) {
		set(gc);
	}

	public datetime(date d) {
		set(d != null ? d.get() : date.MIN.get());
	}

	public datetime(datetime d) {
		set(d != null ? d.get() : date.MIN.get());
	}

	public datetime(java.sql.Date datetime) {
		set(datetime);
	}

	public datetime(java.util.Date datetime) {
		set(datetime.getTime());
	}

	public datetime(java.sql.Timestamp datetime) {
		set(datetime.getTime());
	}

	public datetime(String datetime) {
		if(datetime == null || datetime.isEmpty()) {
			set(MIN);
			return;
		}

		// yyyy-MM-ddThh:mm[:ss][+hh:mm[:ss]]
		// 0123456789012345 678  901234 567

		// yyyy-MM-ddThh:mm[:ss].000Z
		// 0123456789012345 678 90123

		int length = datetime.length(); 
		int year = Integer.parseInt(datetime.substring(0, 4));
		int month = Integer.parseInt(datetime.substring(5, 7));
		int day = Integer.parseInt(datetime.substring(8, 10));
		int hours = Integer.parseInt(datetime.substring(11, 13));
		int minutes = Integer.parseInt(datetime.substring(14, 16));

		boolean hasSeconds = length > 17 && datetime.charAt(16) == ':'; 
		int seconds = hasSeconds ? Integer.parseInt(datetime.substring(17, 19)) : 0;

		int shift = hasSeconds ? 0 : 3;

		int index = 19 - shift;

		int zoneOffset = 0;
		boolean hasZone = index < length && datetime.charAt(index) != '.';

		if(hasZone) {
			int zoneSign = datetime.charAt(index) == '+' ? 1 : -1;

			index = 20 - shift;
			int zoneHours = Integer.parseInt(datetime.substring(index, index + 2));

			index = 23 - shift;
			int zoneMinutes = Integer.parseInt(datetime.substring(index, index + 2));

			index = 25 - shift;
			boolean hasZoneSeconds = index < length;

			index = 26 - shift;
			int zoneSeconds = hasZoneSeconds ? Integer.parseInt(datetime.substring(index, index + 2)) : 0;

			zoneOffset = zoneSign * zoneHours * datespan.TicksPerHour + zoneMinutes * datespan.TicksPerMinute + zoneSeconds * datespan.TicksPerSecond;
		}

		set(year, month, day, hours, minutes, seconds);
		setZoneOffset(zoneOffset);
	}

	public datetime(String s, String format) {
		set(s, format);
	}

	public datetime(String s, String[] formats) {
		for(int i = 0; i < formats.length; i++) {
			try {
				set(s, formats[i]);
			} catch(Throwable e) {
			}
		}
	}

	@Override
	public datetime defaultValue() {
		return new datetime(MIN);
	}

	public GregorianCalendar get() {
		return value;
	}

	public long getTicks() {
		return value.getTimeInMillis();
	}

	private void set(long milliseconds) {
		value.setTimeInMillis(milliseconds);
	}

	public void set(datetime datetime) {
		set(datetime.get());
	}

	public void set(date date) {
		set(date.get());
	}

	public void set(GregorianCalendar gc) {
		set(gc.getTimeInMillis());
	}

	public void set(java.sql.Date datetime) {
		if(datetime != null)
			set(datetime.getTime());
	}

	public void set(java.util.Date datetime) {
		if(datetime != null)
			set(datetime.getTime());
	}

	public boolean set(int year, int month, int day, int hour, int minute, int second) {
		return set(year, month, day, hour, minute, second, 0);
	}

	public boolean set(int year, int month, int day, int hour, int minute, int second, int millisecond) {
		try {
			value.set(GregorianCalendar.YEAR, year);
			value.set(GregorianCalendar.MONTH, month - 1);
			value.set(GregorianCalendar.DAY_OF_MONTH, day);
			value.set(GregorianCalendar.HOUR_OF_DAY, hour);
			value.set(GregorianCalendar.MINUTE, minute);
			value.set(GregorianCalendar.SECOND, second);
			value.set(GregorianCalendar.MILLISECOND, millisecond);
		} catch(Throwable e) {
			return false;
		}
		return true;
	}

	public void set(String s, String format) {
		try {
			if(s == null || s.isEmpty()) {
				set(datetime.MIN);
			} else {
				Date date = new SimpleDateFormat(format).parse(s);
				set(date.getTime());
			}
		} catch(ParseException e) {
			throw new exception(e);
		}
	}

	public boolean setDate(int year, int month, int day) {
		return set(year, month, day, hour(), minute(), second());
	}

	public boolean setTime(int hour, int minute, int second) {
		return setTime(hour, minute, second, 0);
	}

	public void setZoneOffset(int zoneOffset) {
		value.set(GregorianCalendar.ZONE_OFFSET, zoneOffset);
	}

	public boolean setTime(int hour, int minute, int second, int millisecond) {
		try {
			value.set(GregorianCalendar.HOUR, hour);
			value.set(GregorianCalendar.MINUTE, minute);
			value.set(GregorianCalendar.SECOND, second);
			value.set(GregorianCalendar.MILLISECOND, millisecond);
		} catch(Throwable e) {
			return false;
		}
		return true;
	}

	public int year() {
		return value.get(GregorianCalendar.YEAR);
	}

	public int quarter() {
		return value.get(GregorianCalendar.MONTH) / 3 + 1;
	}

	public int month() {
		return value.get(GregorianCalendar.MONTH) + 1;
	}

	public int day() {
		return value.get(GregorianCalendar.DAY_OF_MONTH);
	}

	public int dayOfWeek() {
		return value.get(GregorianCalendar.DAY_OF_WEEK);
	}

	public int dayOfYear() {
		return value.get(GregorianCalendar.DAY_OF_YEAR);
	}

	public int hour() {
		return value.get(GregorianCalendar.HOUR_OF_DAY);
	}

	public int minute() {
		return value.get(GregorianCalendar.MINUTE);
	}

	public int second() {
		return value.get(GregorianCalendar.SECOND);
	}

	public int millisecond() {
		return value.get(GregorianCalendar.MILLISECOND);
	}

	public int zoneOffset() {
		return value.get(GregorianCalendar.ZONE_OFFSET);
	}

	public datetime addYear(int years) {
		GregorianCalendar calendar = (GregorianCalendar)value.clone();
		calendar.add(GregorianCalendar.YEAR, years);
		return new datetime(calendar);
	}

	public datetime addQuarter(int quarters) {
		GregorianCalendar calendar = (GregorianCalendar)value.clone();
		calendar.add(GregorianCalendar.MONTH, 3 * quarters);
		return new datetime(calendar);
	}

	public datetime addMonth(int months) {
		GregorianCalendar calendar = (GregorianCalendar)value.clone();
		calendar.add(GregorianCalendar.MONTH, months);
		return new datetime(calendar);
	}

	public datetime addDay(int days) {
		GregorianCalendar calendar = (GregorianCalendar)value.clone();
		calendar.add(GregorianCalendar.DAY_OF_MONTH, days);
		return new datetime(calendar);
	}

	public datetime addHour(int hours) {
		GregorianCalendar calendar = (GregorianCalendar)value.clone();
		calendar.add(GregorianCalendar.HOUR, hours);
		return new datetime(calendar);
	}

	public datetime addMinute(int minutes) {
		GregorianCalendar calendar = (GregorianCalendar)value.clone();
		calendar.add(GregorianCalendar.MINUTE, minutes);
		return new datetime(calendar);
	}

	public datetime addSecond(int seconds) {
		GregorianCalendar calendar = (GregorianCalendar)value.clone();
		calendar.add(GregorianCalendar.SECOND, seconds);
		return new datetime(calendar);
	}

	public datetime truncYear() {
		return new datetime(new GregorianCalendar(value.get(GregorianCalendar.YEAR), 0, 1));
	}

	public datetime truncQuarter() {
		int month = value.get(GregorianCalendar.MONTH);
		return new datetime(new GregorianCalendar(value.get(GregorianCalendar.YEAR), month - month % 3, 1));
	}

	public datetime truncMonth() {
		return new datetime(new GregorianCalendar(value.get(GregorianCalendar.YEAR), value.get(GregorianCalendar.MONTH), 1));
	}

	public datetime truncDay() {
		return new datetime(new GregorianCalendar(value.get(GregorianCalendar.YEAR), value.get(GregorianCalendar.MONTH), value.get(GregorianCalendar.DAY_OF_MONTH)));
	}

	public datetime truncHour() {
		return new datetime(new GregorianCalendar(value.get(GregorianCalendar.YEAR), value.get(GregorianCalendar.MONTH), value.get(GregorianCalendar.DAY_OF_MONTH), value.get(GregorianCalendar.HOUR), 0, 0));
	}

	public datetime truncMinute() {
		return new datetime(new GregorianCalendar(value.get(GregorianCalendar.YEAR), value.get(GregorianCalendar.MONTH), value.get(GregorianCalendar.DAY_OF_MONTH), value.get(GregorianCalendar.HOUR), value.get(GregorianCalendar.MINUTE), 0));
	}

	@Override
	public String toString() {
		int day = day();
		int month = month();
		int year = year();

		int hours = hour();
		int minutes = minute();
		int seconds = second();

		long offset = zoneOffset();

		long offsetHours = Math.abs(offset / datespan.TicksPerHour);
		long offsetMinutes = Math.abs((offset % datespan.TicksPerHour) / datespan.TicksPerMinute);
		long offsetSeconds = Math.abs((offset % datespan.TicksPerHour) % datespan.TicksPerMinute / datespan.TicksPerSecond);

		String result = "" + (year < 10 ? "000" : (year < 100 ? "00" : (year < 1000 ? "0" : ""))) + year +
				'-' + (month < 10 ? "0" + month : month) + 
				'-' + (day < 10 ? "0" + day : day) + 
				"T" + (hours < 10 ? "0" + hours : hours) + ":" + (minutes < 10 ? "0" + minutes : minutes);

		if(seconds != 0)
			result += ":" + (seconds < 10 ? "0" + seconds : seconds);

		if(offsetHours != 0 || offsetMinutes != 0 || offsetSeconds != 0) {
			result += "" + (offset > 0 ? '+' : '-') +
				(offsetHours < 10 ? "0" + offsetHours : offsetHours) + ":" +
				(offsetMinutes < 10 ? "0" + offsetMinutes : offsetMinutes);
		}

		if(offsetSeconds != 0)
			result += ":" + (offsetSeconds < 10 ? "0" + offsetSeconds : offsetSeconds);

		return result;
	}

	public String format(String format) {
		return format(new SimpleDateFormat(format));
	}

	public String format(DateFormat format) {
		return format.format(value.getTime());
	}

	@Override
	public FieldType type() {
		return FieldType.Datetime;
	}

	@Override
	public String toDbConstant(DatabaseVendor vendor) {
		if(vendor != DatabaseVendor.Postgres)
			throw new UnsupportedOperationException();

		return new ToDatetime(new SqlStringToken("'" + toString() + "'")).format(vendor, new FormatOptions());
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object d) {
		if(d instanceof datetime) {
			return operatorEqu((datetime)d).get();
		}
		return false;
	}

	public sql_datetime sql_datetime() {
		return new sql_datetime(this);
	}

	public void operatorAssign(date value) {
		set(value);
	}

	public void operatorAssign(datetime value) {
		set(value);
	}

	public datetime operatorAdd(datespan x) {
		return new datetime(value.getTimeInMillis() + x.milliseconds());
	}

	public datespan operatorSub(date x) {
		return new datespan(value.getTimeInMillis() - x.get().getTimeInMillis());
	}

	public datetime operatorSub(datespan x) {
		return new datetime(value.getTimeInMillis() - x.milliseconds());
	}

	public datespan operatorSub(datetime x) {
		return new datespan(value.getTimeInMillis() - x.get().getTimeInMillis());
	}

	public datetime operatorAddAssign(datespan x) {
		set(operatorAdd(x));
		return this;
	}

	public datetime operatorSubAssign(datespan x) {
		set(operatorSub(x));
		return this;
	}

	public bool operatorEqu(date x) {
		return operatorEqu(x.datetime());
	}

	public bool operatorNotEqu(date x) {
		return operatorNotEqu(x.datetime());
	}

	public bool operatorLess(date x) {
		return operatorLess(x.datetime());
	}

	public bool operatorMore(date x) {
		return operatorMore(x.datetime());
	}

	public bool operatorLessEqu(date x) {
		return operatorLessEqu(x.datetime());
	}

	public bool operatorMoreEqu(date x) {
		return operatorMoreEqu(x.datetime());
	}

	public bool operatorEqu(datetime x) {
		return new bool(value.compareTo(x.get()) == 0);
	}

	public bool operatorNotEqu(datetime x) {
		return new bool(value.compareTo(x.get()) != 0);
	}

	public bool operatorLess(datetime x) {
		return new bool(value.compareTo(x.get()) < 0);
	}

	public bool operatorMore(datetime x) {
		return new bool(value.compareTo(x.get()) > 0);
	}

	public bool operatorLessEqu(datetime x) {
		return new bool(value.compareTo(x.get()) <= 0);
	}

	public bool operatorMoreEqu(datetime x) {
		return new bool(value.compareTo(x.get()) >= 0);
	}

	static public datetime z8_now() {
		return new datetime();
	}

	@Override
	public date z8_toDate() {
		return new date(this);
	}

	public integer z8_year() {
		return new integer(year());
	}

	public integer z8_quarter() {
		return new integer(quarter());
	}

	public integer z8_month() {
		return new integer(month());
	}

	public integer z8_day() {
		return new integer(day());
	}

	public integer z8_dayOfWeek() {
		return new integer(dayOfWeek());
	}

	public integer z8_dayOfYear() {
		return new integer(dayOfYear());
	}

	public integer z8_hour() {
		return new integer(hour());
	}

	public integer z8_minute() {
		return new integer(minute());
	}

	public integer z8_second() {
		return new integer(second());
	}

	public integer z8_millisecond() {
		return new integer(millisecond());
	}

	public void z8_set(integer year, integer month, integer day, integer hour, integer minute, integer second) {
		set(year.getInt(), month.getInt(), day.getInt(), hour.getInt(), minute.getInt(), second.getInt());
	}

	public void z8_set(integer year, integer month, integer day, integer hour, integer minute, integer second, integer millisecond) {
		set(year.getInt(), month.getInt(), day.getInt(), hour.getInt(), minute.getInt(), second.getInt(), millisecond.getInt());
	}

	public void z8_setDate(integer year, integer month, integer day) {
		setDate(year.getInt(), month.getInt(), day.getInt());
	}

	public void z8_setTime(integer hour, integer minute, integer second) {
		setTime(hour.getInt(), minute.getInt(), second.getInt());
	}

	public void z8_setTime(integer hour, integer minute, integer second, integer millisecond) {
		setTime(hour.getInt(), minute.getInt(), second.getInt(), millisecond.getInt());
	}

	public datetime z8_addYear(integer years) {
		return addYear(years.getInt());
	}

	public datetime z8_addQuarter(integer quarters) {
		return addQuarter(quarters.getInt());
	}

	public datetime z8_addMonth(integer months) {
		return addMonth(months.getInt());
	}

	public datetime z8_addDay(integer days) {
		return addDay(days.getInt());
	}

	public datetime z8_addHour(integer hours) {
		return addHour(hours.getInt());
	}

	public datetime z8_addMinute(integer minutes) {
		return addMinute(minutes.getInt());
	}

	public datetime z8_addSecond(integer seconds) {
		return addSecond(seconds.getInt());
	}

	public datetime z8_truncYear() {
		return truncYear();
	}

	public datetime z8_truncQuarter() {
		return truncQuarter();
	}

	public datetime z8_truncMonth() {
		return truncMonth();
	}

	public datetime z8_truncDay() {
		return truncDay();
	}

	public datetime z8_truncHour() {
		return truncHour();
	}

	public datetime z8_truncMinute() {
		return truncMinute();
	}

	public string z8_toString(string format) {
		return new string(format(format.get()));
	}

	static public datetime z8_parse(string string) {
		return new datetime(string.get());
	}

	static public datetime z8_parse(string string, string format) {
		return new datetime(string.get(), format.get());
	}
}
