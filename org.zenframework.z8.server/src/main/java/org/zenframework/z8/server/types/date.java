package org.zenframework.z8.server.types;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.functions.conversion.ToDate;
import org.zenframework.z8.server.types.sql.sql_date;

public final class date extends primary {

	private static final long serialVersionUID = 9203264055660935905L;

	private GregorianCalendar m_value = new GregorianCalendar();

	final static public date MIN = new date(1899, 12, 31);
	final static public date MAX = new date(4712, 12, 31);

	public date() {
		nullTime();
	}

	public date(date date) {
		this(date.get());
	}

	public date(datetime date) {
		this(date.get());
	}

	public date(java.sql.Date date) {
		set(date);
	}

	public date(java.sql.Timestamp datetime) {
		set(datetime.getTime());
	}

	public date(long milliseconds) {
		set(milliseconds);
	}

	public date(int year, int month, int day) {
		set(year, month, day);
	}

	public date(GregorianCalendar gc) {
		set(gc);
	}

	public date(String date) {
		if (date != null && !date.isEmpty()) {
			// dd/mm/yyyy
			// 0123456789

			int day = Integer.parseInt(date.substring(0, 2));
			int month = Integer.parseInt(date.substring(3, 5));
			int year = Integer.parseInt(date.substring(6, 10));

			set(year, month, day);
		} else
			set(MIN);
	}

	public date(String s, String format) {
		set(s, format);
	}

	public date(String s, String[] formats) {
		for (int i = 0; i < formats.length; i++) {
			try {
				set(s, formats[i]);
			} catch (Throwable e) {}
		}
	}

	@Override
	public date defaultValue() {
		return new date();
	}

	private void nullTime() {
		m_value.set(GregorianCalendar.HOUR_OF_DAY, 0);
		m_value.set(GregorianCalendar.MINUTE, 0);
		m_value.set(GregorianCalendar.SECOND, 0);
		m_value.set(GregorianCalendar.MILLISECOND, 0);
	}

	public long getTicks() {
		return m_value.getTimeInMillis();
	}

	public GregorianCalendar get() {
		return m_value;
	}

	public void set(date date) {
		set(date.get());
	}

	public void set(java.sql.Date date) {
		set(date.getTime());
	}

	public void set(GregorianCalendar gc) {
		set(gc.getTimeInMillis());
	}

	private void set(long milliseconds) {
		m_value.setTimeInMillis(milliseconds);
		nullTime();
	}

	public boolean set(int year, int month, int day) {
		try {
			m_value.set(year, month - 1, day);
			nullTime();
		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	public void set(String s, String format) {
		try {
			if (s.isEmpty()) {
				set(date.MIN);
			} else {
				java.util.Date date = new SimpleDateFormat(format).parse(s);
				set(1900 + date.getYear(), date.getMonth() + 1, date.getDate());
			}
		} catch (ParseException e) {
			throw new exception(e);
		}
	}

	@Override
	public String toString() {
		int day = day();
		int month = month();
		int year = year();
		return (day < 10 ? "0" + day : day) + "/" + (month < 10 ? "0" + month : month) + "/" + year;
	}

	public String format(String format) {
		return new SimpleDateFormat(format).format(m_value.getTime());
	}

	@Override
	public FieldType type() {
		return FieldType.Date;
	}

	@Override
	public String toDbConstant(DatabaseVendor vendor) {
		return new ToDate(new SqlStringToken("'" + toString() + "'")).format(vendor, new FormatOptions());
	}

	@Override
	public int hashCode() {
		return m_value.hashCode();
	}

	@Override
	public boolean equals(Object d) {
		if (d instanceof date) {
			return operatorEqu((date) d).get();
		}
		return false;
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

	public int weekOfYear() {
		m_value.setMinimalDaysInFirstWeek(7);
		return m_value.get(GregorianCalendar.WEEK_OF_YEAR);
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

	public date addYear(int years) {
		GregorianCalendar value = (GregorianCalendar) m_value.clone();
		value.add(GregorianCalendar.YEAR, years);
		return new date(value);
	}

	public date addQuarter(int quarters) {
		GregorianCalendar value = (GregorianCalendar) m_value.clone();
		value.add(GregorianCalendar.MONTH, 3 * quarters);
		return new date(value);
	}

	public date addMonth(int months) {
		GregorianCalendar value = (GregorianCalendar) m_value.clone();
		value.add(GregorianCalendar.MONTH, months);
		return new date(value);
	}

	public date addDay(int days) {
		GregorianCalendar value = (GregorianCalendar) m_value.clone();
		value.add(GregorianCalendar.DAY_OF_MONTH, days);
		return new date(value);
	}

	public date truncYear() {
		return new date(new GregorianCalendar(m_value.get(GregorianCalendar.YEAR), 0, 1));
	}

	public date truncQuarter() {
		int month = m_value.get(GregorianCalendar.MONTH);
		return new date(new GregorianCalendar(m_value.get(GregorianCalendar.YEAR), month - month % 3, 1));
	}

	public date truncMonth() {
		return new date(new GregorianCalendar(m_value.get(GregorianCalendar.YEAR), m_value.get(GregorianCalendar.MONTH), 1));
	}

	public date truncDay() {
		return new date(new GregorianCalendar(m_value.get(GregorianCalendar.YEAR), m_value.get(GregorianCalendar.MONTH),
				m_value.get(GregorianCalendar.DAY_OF_MONTH)));
	}

	@Override
	public datetime datetime() {
		return new datetime(this);
	}

	public sql_date sql_date() {
		return new sql_date(this);
	}

	public void operatorAssign(date value) {
		set(value);
	}

	public datetime operatorAdd(datespan x) {
		return new datetime(this).operatorAdd(x);
	}

	public datetime operatorSub(datespan x) {
		return datetime().operatorSub(x);
	}

	public datespan operatorSub(date x) {
		return datetime().operatorSub(x);
	}

	public datespan operatorSub(datetime x) {
		return datetime().operatorSub(x);
	}

	public bool operatorEqu(date x) {
		return datetime().operatorEqu(x);
	}

	public bool operatorNotEqu(date x) {
		return datetime().operatorNotEqu(x);
	}

	public bool operatorLess(date x) {
		return datetime().operatorLess(x);
	}

	public bool operatorMore(date x) {
		return datetime().operatorMore(x);
	}

	public bool operatorLessEqu(date x) {
		return datetime().operatorLessEqu(x);
	}

	public bool operatorMoreEqu(date x) {
		return datetime().operatorMoreEqu(x);
	}

	public bool operatorEqu(datetime x) {
		return datetime().operatorEqu(x);
	}

	public bool operatorNotEqu(datetime x) {
		return datetime().operatorNotEqu(x);
	}

	public bool operatorLess(datetime x) {
		return datetime().operatorLess(x);
	}

	public bool operatorMore(datetime x) {
		return datetime().operatorMore(x);
	}

	public bool operatorLessEqu(datetime x) {
		return datetime().operatorLessEqu(x);
	}

	public bool operatorMoreEqu(datetime x) {
		return datetime().operatorMoreEqu(x);
	}

	static public date z8_today() {
		return new date();
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

	public integer z8_weekOfYear() {
		return new integer(weekOfYear());
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

	public void z8_set(integer year, integer month, integer day) {
		set(year.getInt(), month.getInt(), day.getInt());
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

	public date z8_truncYear() {
		return truncYear();
	}

	public date z8_truncQuarter() {
		return truncQuarter();
	}

	public date z8_truncMonth() {
		return truncMonth();
	}

	public date z8_truncDay() {
		return truncDay();
	}

	public string z8_toString(string frm) {
		return new string(format(frm.get()));
	}

	static public date z8_parse(string string) {
		return new date(string.get());
	}

	static public date z8_parse(string string, string format) {
		return new date(string.get(), format.get());
	}
}
