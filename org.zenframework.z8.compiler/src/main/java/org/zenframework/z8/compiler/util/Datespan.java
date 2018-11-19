package org.zenframework.z8.compiler.util;

public final class Datespan{

	private long ticks = 0;

	static final public int TicksPerSecond = 1000;
	static final public int TicksPerMinute = TicksPerSecond * 60;
	static final public int TicksPerHour = TicksPerMinute * 60;
	static final public int TicksPerDay = TicksPerHour * 24;

	public Datespan() {
	}

	public Datespan(long ticks) {
		set(ticks);
	}

	public Datespan(Datespan dateSpan) {
		set(dateSpan.ticks);
	}

	public Datespan(String datespan) {
		// 01234567890
		// d hh:mm:ss
		// hh:mm:ss
		// mm:ss
		// ss
		// s
		// 01234567890

		int length = datespan.length();

		if(length < 3) {
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

	public Datespan(int days, int hours, int minutes, int seconds) {
		this(days, hours, minutes, seconds, 0);
	}

	public Datespan(int days, int hours, int minutes, int seconds, long milliseconds) {
		set(days, hours, minutes, seconds, milliseconds);
	}

	public long getTicks() {
		return ticks;
	}

	public void set(Datespan span) {
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

	@Override
	public String toString() {
		long days = days();
		long hours = hours();
		long minutes = minutes();
		long seconds = seconds();
		return "" + days + " " + (hours < 10 ? "0" : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
	}
}
