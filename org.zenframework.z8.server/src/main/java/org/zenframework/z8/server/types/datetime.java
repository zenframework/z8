package org.zenframework.z8.server.types;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.functions.conversion.ToDatetime;
import org.zenframework.z8.server.types.sql.sql_datetime;

public class datetime extends primary {

	private static final long serialVersionUID = -5362639596768531077L;

	final static public String defaultMask = "dd/MM/yyyy HH:mm:ss";
	final static public String defaultMaskDate = "dd/MM/yyyy";
	final static public String defaultMaskTime = "HH:mm:ss";

	final static public datetime MIN = new datetime(1899, 12, 31);
	final static public datetime MAX = new datetime(4712, 12, 31);

	final static public String[] knownFormats = new String[] { "dd/MM/yyyy HH:mm:ss", "dd/MM/yyyy HH:mm",
			"dd.MM.yyyy HH:mm:ss", "dd.MM.yyyy HH:mm", "dd/MM/yyyy HH-mm-ss", "dd.MM.yyyy HH-mm-ss", "dd/MM/yyyy HH-mm",
			"dd.MM.yyyy HH-mm", "dd.MM.yyyy", "dd/MM/yyyy", "HH:mm:ss", "HH:mm", "HH-mm-ss", "HH-mm" };

	protected GregorianCalendar m_value = new GregorianCalendar();

	public datetime() {}

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

	public datetime(date date) {
		set(date);
	}

	public datetime(datetime datetime) {
		set(datetime);
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

	public datetime(String s) {
		this(s, defaultMask);
	}

	public datetime(String s, String format) {
		set(s, format);
	}

	public datetime(String s, String[] formats) {
		for (int i = 0; i < formats.length; i++) {
			try {
				set(s, formats[i]);
			} catch (Throwable e) {}
		}
	}

	@Override
	public datetime defaultValue() {
		return new datetime(MIN);
	}

	public GregorianCalendar get() {
		return m_value;
	}

	public long getTicks() {
		return m_value.getTimeInMillis();
	}

	public void set(long millisec) {
		m_value.setTimeInMillis(millisec);
	}

	public void set(datetime date) {
		set(date.get());
	}

	public void set(date date) {
		set(date.get());
	}

	public void set(GregorianCalendar gc) {
		set(gc.getTimeInMillis());
	}

	public void set(java.sql.Date datetime) {
		if (datetime != null)
			set(datetime.getTime());
	}

	public void set(java.util.Date datetime) {
		if (datetime != null)
			set(datetime.getTime());
	}

	public boolean set(int year, int month, int day, int hour, int minute, int second) {
		return set(year, month, day, hour, minute, second, 0);
	}

	public boolean set(int year, int month, int day, int hour, int minute, int second, int millisecond) {
		try {
			m_value.set(GregorianCalendar.YEAR, year);
			m_value.set(GregorianCalendar.MONTH, month - 1);
			m_value.set(GregorianCalendar.DAY_OF_MONTH, day);
			m_value.set(GregorianCalendar.HOUR_OF_DAY, hour);
			m_value.set(GregorianCalendar.MINUTE, minute);
			m_value.set(GregorianCalendar.SECOND, second);
			m_value.set(GregorianCalendar.MILLISECOND, millisecond);
		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	public void set(String s, String format) {
		try {
			if (s == null || s.isEmpty()) {
				set(datetime.MIN);
			} else {
				java.util.Date date = new SimpleDateFormat(format).parse(s);
				set(1900 + date.getYear(), date.getMonth() + 1, date.getDate(), date.getHours(), date.getMinutes(),
						date.getSeconds());
			}
		} catch (ParseException e) {
			throw new exception(e);
		}
	}

	public boolean setDate(int year, int month, int day) {
		return set(year, month, day, hour(), minute(), second());
	}

	public boolean setTime(int hour, int minute, int second) {
		return setTime(hour, minute, second, 0);
	}

	public boolean setTime(int hour, int minute, int second, int millisecond) {
		try {
			m_value.set(GregorianCalendar.HOUR, hour);
			m_value.set(GregorianCalendar.MINUTE, minute);
			m_value.set(GregorianCalendar.SECOND, second);
			m_value.set(GregorianCalendar.MILLISECOND, millisecond);
		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	public int year() {
		return m_value.get(GregorianCalendar.YEAR);
	}

	public int quarter() {
		return m_value.get(GregorianCalendar.MONTH) / 3 + 1;
	}

	public int month() {
		return m_value.get(GregorianCalendar.MONTH) + 1;
	}

	public int day() {
		return m_value.get(GregorianCalendar.DAY_OF_MONTH);
	}

	public int dayOfWeek() {
		return m_value.get(GregorianCalendar.DAY_OF_WEEK);
	}

	public int dayOfYear() {
		return m_value.get(GregorianCalendar.DAY_OF_YEAR);
	}

	public int hour() {
		return m_value.get(GregorianCalendar.HOUR_OF_DAY);
	}

	public int minute() {
		return m_value.get(GregorianCalendar.MINUTE);
	}

	public int second() {
		return m_value.get(GregorianCalendar.SECOND);
	}

	public int millisecond() {
		return m_value.get(GregorianCalendar.MILLISECOND);
	}

	public datetime addYear(int years) {
		GregorianCalendar value = (GregorianCalendar) m_value.clone();
		value.add(GregorianCalendar.YEAR, years);
		return new datetime(value);
	}

	public datetime addQuarter(int quarters) {
		GregorianCalendar value = (GregorianCalendar) m_value.clone();
		value.add(GregorianCalendar.MONTH, 3 * quarters);
		return new datetime(value);
	}

	public datetime addMonth(int months) {
		GregorianCalendar value = (GregorianCalendar) m_value.clone();
		value.add(GregorianCalendar.MONTH, months);
		return new datetime(value);
	}

	public datetime addDay(int days) {
		GregorianCalendar value = (GregorianCalendar) m_value.clone();
		value.add(GregorianCalendar.DAY_OF_MONTH, days);
		return new datetime(value);
	}

	public datetime addHour(int hours) {
		GregorianCalendar value = (GregorianCalendar) m_value.clone();
		value.add(GregorianCalendar.HOUR, hours);
		return new datetime(value);
	}

	public datetime addMinute(int minutes) {
		GregorianCalendar value = (GregorianCalendar) m_value.clone();
		value.add(GregorianCalendar.MINUTE, minutes);
		return new datetime(value);
	}

	public datetime addSecond(int seconds) {
		GregorianCalendar value = (GregorianCalendar) m_value.clone();
		value.add(GregorianCalendar.SECOND, seconds);
		return new datetime(value);
	}

	public datetime truncYear() {
		return new datetime(new GregorianCalendar(m_value.get(GregorianCalendar.YEAR), 0, 1));
	}

	public datetime truncQuarter() {
		int month = m_value.get(GregorianCalendar.MONTH);
		return new datetime(new GregorianCalendar(m_value.get(GregorianCalendar.YEAR), month - month % 3, 1));
	}

	public datetime truncMonth() {
		return new datetime(new GregorianCalendar(m_value.get(GregorianCalendar.YEAR), m_value.get(GregorianCalendar.MONTH),
				1));
	}

	public datetime truncDay() {
		return new datetime(new GregorianCalendar(m_value.get(GregorianCalendar.YEAR), m_value.get(GregorianCalendar.MONTH),
				m_value.get(GregorianCalendar.DAY_OF_MONTH)));
	}

	public datetime truncHour() {
		return new datetime(new GregorianCalendar(m_value.get(GregorianCalendar.YEAR), m_value.get(GregorianCalendar.MONTH),
				m_value.get(GregorianCalendar.DAY_OF_MONTH), m_value.get(GregorianCalendar.HOUR), 0, 0));
	}

	public datetime truncMinute() {
		return new datetime(new GregorianCalendar(m_value.get(GregorianCalendar.YEAR), m_value.get(GregorianCalendar.MONTH),
				m_value.get(GregorianCalendar.DAY_OF_MONTH), m_value.get(GregorianCalendar.HOUR),
				m_value.get(GregorianCalendar.MINUTE), 0));
	}

	@Override
	public String toString() {
		return format(defaultMask);
	}

	public String toStringDate() {
		return format(defaultMaskDate);
	}

	public String toStringTime() {
		return format(defaultMaskTime);
	}

	public String format(String format) {
		return new SimpleDateFormat(format).format(m_value.getTime());
	}

	public String format(DateFormat format) {
		return format.format(m_value.getTime());
	}

	@Override
	public FieldType type() {
		return FieldType.Datetime;
	}

	@Override
	public String toDbConstant(DatabaseVendor vendor) {
		return new ToDatetime(new SqlStringToken("'" + toString() + "'")).format(vendor, new FormatOptions());
	}

	@Override
	public int hashCode() {
		return m_value.hashCode();
	}

	@Override
	public boolean equals(Object d) {
		if (d instanceof datetime) {
			return operatorEqu((datetime) d).get();
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
		return new datetime(m_value.getTimeInMillis() + x.milliseconds());
	}

	public datespan operatorSub(date x) {
		return new datespan(m_value.getTimeInMillis() - x.get().getTimeInMillis());
	}

	public datetime operatorSub(datespan x) {
		return new datetime(m_value.getTimeInMillis() - x.milliseconds());
	}

	public datespan operatorSub(datetime x) {
		return new datespan(m_value.getTimeInMillis() - x.get().getTimeInMillis());
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
		return new bool(m_value.compareTo(x.get()) == 0);
	}

	public bool operatorNotEqu(datetime x) {
		return new bool(m_value.compareTo(x.get()) != 0);
	}

	public bool operatorLess(datetime x) {
		return new bool(m_value.compareTo(x.get()) < 0);
	}

	public bool operatorMore(datetime x) {
		return new bool(m_value.compareTo(x.get()) > 0);
	}

	public bool operatorLessEqu(datetime x) {
		return new bool(m_value.compareTo(x.get()) <= 0);
	}

	public bool operatorMoreEqu(datetime x) {
		return new bool(m_value.compareTo(x.get()) >= 0);
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

	public void z8_set(integer year, integer month, integer day, integer hour, integer minute, integer second,
			integer millisecond) {
		set(year.getInt(), month.getInt(), day.getInt(), hour.getInt(), minute.getInt(), second.getInt(),
				millisecond.getInt());
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

	public string z8_toStringDate() {
		return new string(toStringDate());
	}

	public string z8_toStringTime() {
		return new string(toStringTime());
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
