package org.zenframework.z8.compiler.util;

import java.util.GregorianCalendar;
import java.util.TimeZone;

public class Date {
	public static long UtcMin = -62135769600000l;
	public static long UtcMax = 95617584000000l;

	final static public String DefaultTimeZoneId = "Europe/Moscow";
	final static public Date Min = new Date(UtcMin);
	final static public Date Max = new Date(UtcMax);

	static {
		TimeZone.setDefault(TimeZone.getTimeZone(DefaultTimeZoneId));
	}

	protected GregorianCalendar value = new GregorianCalendar();

	public Date() {
	}

	public Date(int year, int month, int day) {
		set(year, month, day, 0, 0, 0);
	}

	public Date(int year, int month, int day, int hour, int minute, int second) {
		set(year, month, day, hour, minute, second);
	}

	public Date(long milliseconds) {
		setTicks(milliseconds);
	}

	public Date(GregorianCalendar gc) {
		set(gc);
	}

	public Date(Date d) {
		set(d != null ? d.get() : Min.get());
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

	public void set(Date date) {
		set(date.get());
	}

	public void set(GregorianCalendar gc) {
		value = (GregorianCalendar)gc.clone();
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

		long offsetHours = Math.abs(offset / Datespan.TicksPerHour);
		long offsetMinutes = Math.abs((offset % Datespan.TicksPerHour) / Datespan.TicksPerMinute);
/*
		long offsetSeconds = Math.abs((offset % datespan.TicksPerHour) % datespan.TicksPerMinute / datespan.TicksPerSecond);
*/
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
/*
		if(offsetSeconds != 0)
			result += ":" + (offsetSeconds < 10 ? "0" + offsetSeconds : offsetSeconds);
*/
		return result;
	}
}
