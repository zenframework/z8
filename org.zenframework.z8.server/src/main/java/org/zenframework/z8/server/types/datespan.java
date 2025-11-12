package org.zenframework.z8.server.types;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.types.sql.sql_datespan;

public final class datespan extends primary {

	private static final long serialVersionUID = -9105266490701041179L;

	private long ticks = 0;

	static final public int TicksPerSecond = 1000;
	static final public int TicksPerMinute = TicksPerSecond * 60;
	static final public int TicksPerHour = TicksPerMinute * 60;
	static final public int TicksPerDay = TicksPerHour * 24;

	public datespan() {
	}

	public datespan(long ticks) {
		set(ticks);
	}

	public datespan(integer ticks) {
		set(ticks.get());
	}

	public datespan(datespan dateSpan) {
		set(dateSpan.ticks);
	}

	public datespan(String datespan) {
		// 01234567890
		// d hh:mm:ss
		// hh:mm:ss
		// mm:ss
		// ss
		// s
		// 01234567890

		int length = datespan.length();

		if(datespan == null || datespan.isEmpty()) {
			set(0, 0, 0, 0);
		} else if(length < 3) {
			set(0, 0, 0, Integer.parseInt(datespan));
		} else if(length < 6) {
			int minutes = Integer.parseInt(datespan.substring(0, 2));
			int seconds = Integer.parseInt(datespan.substring(3, 5));
			set(0, 0, minutes, seconds);
		} else if(length < 9) {
			int hours = Integer.parseInt(datespan.substring(0, 2));
			int minutes = Integer.parseInt(datespan.substring(3, 5));
			int seconds = Integer.parseInt(datespan.substring(6, 8));
			set(0, hours, minutes, seconds);
		} else {
			int space = datespan.indexOf(" ");
			int days = Integer.parseInt(datespan.substring(0, space));
			int hours = Integer.parseInt(datespan.substring(space + 1, space + 3));
			int minutes = Integer.parseInt(datespan.substring(space + 4, space + 6));
			int seconds = Integer.parseInt(datespan.substring(space + 7, space + 9));
			set(days, hours, minutes, seconds);
		}
	}

	public datespan(int days, int hours, int minutes, int seconds) {
		this(days, hours, minutes, seconds, 0);
	}

	public datespan(int days, int hours, int minutes, int seconds, long milliseconds) {
		set(days, hours, minutes, seconds, milliseconds);
	}

	public long get() {
		return ticks;
	}

	public void set(datespan span) {
		set(span.ticks);
	}

	public void set(int days, int hours, int minutes, int seconds) {
		set(days, hours, minutes, seconds, 0);
	}

	public void set(int days, int hours, int minutes, int seconds, long milliseconds) {
		set(days * TicksPerDay + hours * TicksPerHour + minutes * TicksPerMinute + seconds * TicksPerSecond + milliseconds);
	}

	public void set(long ticks) {
		this.ticks = ticks;
	}

	public long days() {
		return ticks / TicksPerDay;
	}

	public long hours() {
		return ticks / TicksPerHour % 24;
	}

	public long minutes() {
		return ticks / TicksPerMinute % 60;
	}

	public long seconds() {
		return ticks / TicksPerSecond % 60;
	}

	public long milliseconds() {
		return ticks;
	}

	public integer z8_days() {
		return new integer(days());
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

	public integer z8_totalHours() {
		return new integer(ticks / TicksPerHour);
	}

	public integer z8_totalMinutes() {
		return new integer(ticks / TicksPerMinute);
	}

	public integer z8_totalSeconds() {
		return new integer(ticks / TicksPerSecond);
	}

	public void z8_setTotalHours(integer x) {
		ticks = x.get() * TicksPerHour;
	}

	public void z8_setTotalMinutes(integer x) {
		ticks = x.get() * TicksPerMinute;
	}

	public void z8_setTotalSeconds(integer x) {
		ticks = x.get() * TicksPerSecond;
	}

	public void z8_set(integer Day, integer Hour, integer Minute, integer Second) {
		set(Day.getInt(), Hour.getInt(), Minute.getInt(), Second.getInt(), 0);
	}

	public void z8_set(integer Day, integer Hour, integer Minute, integer Second, integer Millisecond) {
		set(Day.getInt(), Hour.getInt(), Minute.getInt(), Second.getInt(), Millisecond.get());
	}

	public void z8_truncDay() {
		set((int)days(), 0, 0, 0, 0);
	}

	public void z8_truncHour() {
		set((int)days(), (int)hours(), 0, 0, 0);
	}

	public void z8_truncMinute() {
		set((int)days(), (int)hours(), (int)minutes(), 0, 0);
	}

	@Override
	public FieldType type() {
		return FieldType.Datespan;
	}

	@Override
	public String toDbConstant(DatabaseVendor dbtype) {
		switch(dbtype) {
		case SqlServer:
			return "(" + z8_milliseconds().toString() + ")";
		default:
			return z8_milliseconds().toString();
		}
	}

	@Override
	public int hashCode() {
		return new Long(ticks).hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if(object instanceof datespan) {
			datespan datespan = (datespan)object;
			return ticks == datespan.ticks;
		}
		return false;
	}

	@Override
	public int compareTo(primary primary) {
		if(primary instanceof datespan) {
			datespan datespan = (datespan)primary;
			return (int)Math.signum(ticks = - datespan.ticks);
		}
		return -1;
	}

	@Override
	public String toString() {
		long days = days();
		long hours = hours();
		long minutes = minutes();
		long seconds = seconds();
		return "" + days + " " + (hours < 10 ? "0" : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
	}

	public sql_datespan sql_datespan() {
		return new sql_datespan(this);
	}

	public date operatorAdd(date x) {
		return new date(x.get().getTimeInMillis() + milliseconds());
	}

	public datespan operatorAdd(datespan x) {
		return new datespan(milliseconds() + x.milliseconds());
	}

	public datespan operatorSub(datespan x) {
		return new datespan(milliseconds() - x.milliseconds());
	}

	public bool operatorEqu(datespan x) {
		return new bool(milliseconds() == x.milliseconds());
	}

	public bool operatorNotEqu(datespan x) {
		return new bool(milliseconds() != x.milliseconds());
	}

	public bool operatorLess(datespan x) {
		return new bool(milliseconds() < x.milliseconds());
	}

	public bool operatorMore(datespan x) {
		return new bool(milliseconds() > x.milliseconds());
	}

	public bool operatorLessEqu(datespan x) {
		return new bool(milliseconds() <= x.milliseconds());
	}

	public bool operatorMoreEqu(datespan x) {
		return new bool(milliseconds() >= x.milliseconds());
	}

	static public datespan z8_parse(string string) {
		return string != null ? new datespan(string.get()) : new datespan(0);
	}

	public string z8_toString(string frm) {
		return new string(toString());
	}
}
