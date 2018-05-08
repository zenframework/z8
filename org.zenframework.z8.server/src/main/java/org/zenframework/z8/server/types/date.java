package org.zenframework.z8.server.types;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.types.sql.sql_date;
import org.zenframework.z8.server.utils.StringUtils;

public class date extends primary {

	private static final long serialVersionUID = -5362639596768531077L;

	public static long UtcMin = -62135769600000l;
	public static long UtcMax = 95617584000000l;

	final static public String DefaultTimeZoneId = "Europe/Moscow";
	final static public date Min = new date(UtcMin);
	final static public date Max = new date(UtcMax);

	static {
		TimeZone.setDefault(TimeZone.getTimeZone(DefaultTimeZoneId));
	}

	protected GregorianCalendar value = new GregorianCalendar();

	public date() {
	}

	public date(int year, int month, int day) {
		set(year, month, day, 0, 0, 0);
	}

	public date(int year, int month, int day, int hour, int minute, int second) {
		set(year, month, day, hour, minute, second);
	}

	public date(long milliseconds) {
		setTicks(milliseconds);
	}

	public date(GregorianCalendar gc) {
		set(gc);
	}

	public date(date d) {
		set(d != null ? d.get() : Min.get());
	}

	public date(java.sql.Date date) {
		set(date);
	}

	public date(java.util.Date date) {
		setTicks(date.getTime());
	}

	public date(java.sql.Timestamp date) {
		setTicks(date.getTime());
	}

	public date(String date) {
		if(date == null || date.isEmpty()) {
			set(Min);
			return;
		}

		// yyyy-MM-ddThh:mm[:ss[.m]][+hh:mm[:ss]]
		// 0123456789012345 678 9?   012345 678

		int length = date.length(); 
		int year = Integer.parseInt(date.substring(0, 4));
		int month = Integer.parseInt(date.substring(5, 7));
		int day = Integer.parseInt(date.substring(8, 10));
		int hours = Integer.parseInt(date.substring(11, 13));
		int minutes = Integer.parseInt(date.substring(14, 16));

		boolean hasSeconds = length > 17 && date.charAt(16) == ':'; 
		int seconds = hasSeconds ? Integer.parseInt(date.substring(17, 19)) : 0;

		int shift = hasSeconds ? 0 : 3;

		int index = 19 - shift;

		int millis = 0;
		boolean hasMillis = length > index && date.charAt(index) == '.';

		if(hasMillis) {
			int offsetStart = StringUtils.indexOfAny(date, index, "-+");
			int millisStart = index + 1;
			int millisEnd = offsetStart != -1 ? offsetStart : length;
			millis = Integer.parseInt(date.substring(millisStart, millisEnd));
			index = millisEnd;
		}

		boolean hasZone = index < length;

		if(hasZone) {
			int zoneSign = date.charAt(index) == '+' ? 1 : -1;

			index += 3;
			int zoneHours = Integer.parseInt(date.substring(index - 2, index));

			index += 3;
			int zoneMinutes = Integer.parseInt(date.substring(index - 2, index));

			index += 3;
			int zoneSeconds = index <= length ? Integer.parseInt(date.substring(index - 2, index)) : 0;

			int zoneOffset = zoneSign * zoneHours * datespan.TicksPerHour + zoneMinutes * datespan.TicksPerMinute + zoneSeconds * datespan.TicksPerSecond;
			setZoneOffset(zoneOffset);
		}

		set(year, month, day, hours, minutes, seconds, millis);
	}

	public date(String s, String format) {
		set(s, format);
	}

	public date(String s, String[] formats) {
		for(int i = 0; i < formats.length; i++) {
			try {
				set(s, formats[i]);
			} catch(Throwable e) {
			}
		}
	}

	public GregorianCalendar get() {
		return value;
	}

	public long getTicks() {
		return value.getTimeInMillis();
	}

	private void setTicks(long milliseconds) {
		value.setTimeInMillis(milliseconds);
	}

	public void set(date date) {
		set(date.get());
	}

	public void set(GregorianCalendar gc) {
		value = (GregorianCalendar)gc.clone();
	}

	public void set(java.sql.Date date) {
		if(date != null)
			setTicks(date.getTime());
	}

	public void set(java.util.Date date) {
		if(date != null)
			setTicks(date.getTime());
	}

	public void set(int year, int month, int day, int hour, int minute, int second) {
		set(year, month, day, hour, minute, second, 0);
	}

	public void set(int year, int month, int day, int hour, int minute, int second, int millisecond) {
		value.set(GregorianCalendar.YEAR, year);
		value.set(GregorianCalendar.MONTH, month - 1);
		value.set(GregorianCalendar.DAY_OF_MONTH, day);
		value.set(GregorianCalendar.HOUR_OF_DAY, hour);
		value.set(GregorianCalendar.MINUTE, minute);
		value.set(GregorianCalendar.SECOND, second);
		value.set(GregorianCalendar.MILLISECOND, millisecond);
	}

	public void set(String s, String format) {
		try {
			if(s == null || s.isEmpty()) {
				set(date.Min);
			} else {
				Date date = new SimpleDateFormat(format).parse(s);
				setTicks(date.getTime());
			}
		} catch(ParseException e) {
			throw new exception(e);
		}
	}

	public void setDate(int year, int month, int day) {
		set(year, month, day, hours(), minutes(), seconds());
	}

	public void setTime(int hours, int minutes, int seconds) {
		setTime(hours, minutes, seconds, 0);
	}

	public void setZoneOffset(int zoneOffset) {
		value.set(GregorianCalendar.ZONE_OFFSET, zoneOffset);
	}

	public void setTime(int hours, int minutes, int seconds, int milliseconds) {
		value.set(GregorianCalendar.HOUR_OF_DAY, hours);
		value.set(GregorianCalendar.MINUTE, minutes);
		value.set(GregorianCalendar.SECOND, seconds);
		value.set(GregorianCalendar.MILLISECOND, milliseconds);
	}

	public int daysInMonth() {
		return value.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
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

	public int weekOfYear() {
		value.setMinimalDaysInFirstWeek(7);
		return value.get(GregorianCalendar.WEEK_OF_YEAR);
	}

	public int day() {
		return value.get(GregorianCalendar.DAY_OF_MONTH);
	}

	public int firstDayOfWeek() {
		return value.getFirstDayOfWeek();
	}

	public int dayOfWeek() {
		return value.get(GregorianCalendar.DAY_OF_WEEK);
	}

	public int dayOfYear() {
		return value.get(GregorianCalendar.DAY_OF_YEAR);
	}

	public int hours() {
		return value.get(GregorianCalendar.HOUR_OF_DAY);
	}

	public int minutes() {
		return value.get(GregorianCalendar.MINUTE);
	}

	public int seconds() {
		return value.get(GregorianCalendar.SECOND);
	}

	public int milliseconds() {
		return value.get(GregorianCalendar.MILLISECOND);
	}

	public int zoneOffset() {
		return value.get(GregorianCalendar.ZONE_OFFSET);
	}

	public date addYear(int years) {
		GregorianCalendar calendar = (GregorianCalendar)value.clone();
		calendar.add(GregorianCalendar.YEAR, years);
		return new date(calendar);
	}

	public date addQuarter(int quarters) {
		GregorianCalendar calendar = (GregorianCalendar)value.clone();
		calendar.add(GregorianCalendar.MONTH, 3 * quarters);
		return new date(calendar);
	}

	public date addMonth(int months) {
		GregorianCalendar calendar = (GregorianCalendar)value.clone();
		calendar.add(GregorianCalendar.MONTH, months);
		return new date(calendar);
	}

	public date addDay(int days) {
		GregorianCalendar calendar = (GregorianCalendar)value.clone();
		calendar.add(GregorianCalendar.DAY_OF_MONTH, days);
		return new date(calendar);
	}

	public date addHour(int hours) {
		GregorianCalendar calendar = (GregorianCalendar)value.clone();
		calendar.add(GregorianCalendar.HOUR_OF_DAY, hours);
		return new date(calendar);
	}

	public date addMinute(int minutes) {
		GregorianCalendar calendar = (GregorianCalendar)value.clone();
		calendar.add(GregorianCalendar.MINUTE, minutes);
		return new date(calendar);
	}

	public date addSecond(int seconds) {
		GregorianCalendar calendar = (GregorianCalendar)value.clone();
		calendar.add(GregorianCalendar.SECOND, seconds);
		return new date(calendar);
	}

	public date addMillisecond(int milliseconds) {
		GregorianCalendar calendar = (GregorianCalendar)value.clone();
		calendar.add(GregorianCalendar.MILLISECOND, milliseconds);
		return new date(calendar);
	}

	public date truncYear() {
		return new date(new GregorianCalendar(value.get(GregorianCalendar.YEAR), 0, 1));
	}

	public date truncQuarter() {
		int month = value.get(GregorianCalendar.MONTH);
		return new date(new GregorianCalendar(value.get(GregorianCalendar.YEAR), month - month % 3, 1));
	}

	public date truncMonth() {
		return new date(new GregorianCalendar(value.get(GregorianCalendar.YEAR), value.get(GregorianCalendar.MONTH), 1));
	}

	public date truncWeek() {
		int firstDayOfWeek = firstDayOfWeek();
		int dayOfWeek = dayOfWeek();
		int days = dayOfWeek < firstDayOfWeek ? dayOfWeek + 7 - firstDayOfWeek : (dayOfWeek - firstDayOfWeek);
		date date = new date(new GregorianCalendar(value.get(GregorianCalendar.YEAR), value.get(GregorianCalendar.MONTH), value.get(GregorianCalendar.DAY_OF_MONTH)));
		return days != 0 ? date.addDay(-days)  : date;
	}

	public date truncDay() {
		return new date(new GregorianCalendar(value.get(GregorianCalendar.YEAR), value.get(GregorianCalendar.MONTH), value.get(GregorianCalendar.DAY_OF_MONTH)));
	}

	public date truncHour() {
		return new date(new GregorianCalendar(value.get(GregorianCalendar.YEAR), value.get(GregorianCalendar.MONTH), value.get(GregorianCalendar.DAY_OF_MONTH), value.get(GregorianCalendar.HOUR_OF_DAY), 0, 0));
	}

	public date truncMinute() {
		return new date(new GregorianCalendar(value.get(GregorianCalendar.YEAR), value.get(GregorianCalendar.MONTH), value.get(GregorianCalendar.DAY_OF_MONTH), value.get(GregorianCalendar.HOUR_OF_DAY), value.get(GregorianCalendar.MINUTE), 0));
	}

	static boolean isEqualDate(date left, date right) {
		return left.year() == right.year() && left.month() == right.month() && left.day() == right.day();
	}
	@Override
	public String toString() {
		return toString("T", "");
	}

	public String toString(String timeSeparator, String zoneSeparator) {
		int day = day();
		int month = month();
		int year = year();

		int hours = hours();
		int minutes = minutes();
		int seconds = seconds();
		int milliseconds = milliseconds();

		long offset = zoneOffset();

		long offsetHours = Math.abs(offset / datespan.TicksPerHour);
		long offsetMinutes = Math.abs((offset % datespan.TicksPerHour) / datespan.TicksPerMinute);
		long offsetSeconds = Math.abs((offset % datespan.TicksPerHour) % datespan.TicksPerMinute / datespan.TicksPerSecond);

		String result = "" + (year < 10 ? "000" : (year < 100 ? "00" : (year < 1000 ? "0" : ""))) + year +
				"-" + (month < 10 ? "0" + month : month) + 
				"-" + (day < 10 ? "0" + day : day) + 
				timeSeparator + (hours < 10 ? "0" + hours : hours) + ":" + (minutes < 10 ? "0" + minutes : minutes);

		result += ":" + (seconds < 10 ? "0" + seconds : seconds);
		if(milliseconds != 0)
			result += "." + (milliseconds < 100 ? (milliseconds < 10 ? "00" : "0") : "") + milliseconds;

		result += zoneSeparator + (offset < 0 ? "-" : "+") +
			(offsetHours < 10 ? "0" + offsetHours : offsetHours) + ":" +
			(offsetMinutes < 10 ? "0" + offsetMinutes : offsetMinutes);

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
		return FieldType.Date;
	}

	@Override
	public String toDbConstant(DatabaseVendor vendor) {
		return "" + getTicks();
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object d) {
		if(d instanceof date) {
			return operatorEqu((date)d).get();
		}
		return false;
	}

	@Override
	public int compareTo(primary primary) {
		if(primary instanceof date) {
			date date = (date)primary;
			return (int)Math.signum(getTicks() - date.getTicks());
		}
		return -1;
	}
	
	public sql_date sql_date() {
		return new sql_date(this);
	}

	public void operatorAssign(date value) {
		set(value);
	}

	public date operatorAdd(datespan x) {
		return new date(value.getTimeInMillis() + x.milliseconds());
	}

	public date operatorSub(datespan x) {
		return new date(value.getTimeInMillis() - x.milliseconds());
	}

	public datespan operatorSub(date x) {
		return new datespan(value.getTimeInMillis() - x.get().getTimeInMillis());
	}

	public date operatorAddAssign(datespan x) {
		set(operatorAdd(x));
		return this;
	}

	public date operatorSubAssign(datespan x) {
		set(operatorSub(x));
		return this;
	}

	public int compare(date x) {
		return value.compareTo(x.get());
	}

	public bool operatorEqu(date x) {
		return new bool(value.compareTo(x.get()) == 0);
	}

	public bool operatorNotEqu(date x) {
		return new bool(value.compareTo(x.get()) != 0);
	}

	public bool operatorLess(date x) {
		return new bool(value.compareTo(x.get()) < 0);
	}

	public bool operatorMore(date x) {
		return new bool(value.compareTo(x.get()) > 0);
	}

	public bool operatorLessEqu(date x) {
		return new bool(value.compareTo(x.get()) <= 0);
	}

	public bool operatorMoreEqu(date x) {
		return new bool(value.compareTo(x.get()) >= 0);
	}

	static public date z8_now() {
		return new date();
	}

	static public date z8_today() {
		return new date();
	}

	static public date z8_yesterday() {
		return z8_today().addDay(-1);
	}

	static public date z8_tomorrow() {
		return z8_today().addDay(1);
	}

	public integer z8_ticks() {
		return new integer(getTicks());
	}

	public integer z8_year() {
		return new integer(year());
	}

	public integer z8_quarter() {
		return new integer(quarter());
	}

	public integer z8_weekOfYear() {
		return new integer(weekOfYear());
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

	public integer z8_daysInMonth() {
		return new integer(daysInMonth());
	}

	public integer z8_hours() {
		return new integer(hours());
	}

	public integer z8_minutes() {
		return new integer(minutes());
	}

	public integer z8_seconds() {
		return new integer(seconds());
	}

	public integer z8_milliseconds() {
		return new integer(milliseconds());
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

	public date z8_addYear(integer years) {
		return addYear(years.getInt());
	}

	public date z8_addQuarter(integer quarters) {
		return addQuarter(quarters.getInt());
	}

	public date z8_addMonth(integer months) {
		return addMonth(months.getInt());
	}

	public date z8_addDay(integer days) {
		return addDay(days.getInt());
	}

	public date z8_addHour(integer hours) {
		return addHour(hours.getInt());
	}

	public date z8_addMinute(integer minutes) {
		return addMinute(minutes.getInt());
	}

	public date z8_addSecond(integer seconds) {
		return addSecond(seconds.getInt());
	}

	public date z8_addMillisecond(integer milliseconds) {
		return addMillisecond(milliseconds.getInt());
	}

	public date z8_truncYear() {
		return truncYear();
	}

	public date z8_truncQuarter() {
		return truncQuarter();
	}

	public date z8_truncMonth() {
		return truncMonth();
	}

	public date z8_truncWeek() {
		return truncWeek();
	}

	public date z8_truncDay() {
		return truncDay();
	}

	public date z8_truncHour() {
		return truncHour();
	}

	public date z8_truncMinute() {
		return truncMinute();
	}

	static public bool z8_isEqualDate(date left, date right) {
		return new bool(isEqualDate(left, right));
	}

	public string z8_toString(string format) {
		return new string(format(format.get()));
	}

	static public date z8_parse(string string) {
		return new date(string.get());
	}

	static public date z8_parse(string string, string format) {
		return new date(string.get(), format.get());
	}
}
