package org.zenframework.z8.compiler.util;

public class Datespan {
    static long TicksPerMillisecond = 10000;
    static long TicksPerSecond = TicksPerMillisecond * 1000;
    static long TicksPerMinute = TicksPerSecond * 60;
    static long TicksPerHour = TicksPerMinute * 60;
    static long TicksPerDay = TicksPerHour * 24;

    static int MillisPerSecond = 1000;
    static int MillisPerMinute = MillisPerSecond * 60;
    static int MillisPerHour = MillisPerMinute * 60;
    static int MillisPerDay = MillisPerHour * 24;

    static long MaxSeconds = Long.MAX_VALUE / TicksPerSecond;
    static long MinSeconds = Long.MIN_VALUE / TicksPerSecond;
    static long MaxMilliSeconds = Long.MAX_VALUE / TicksPerMillisecond;
    static long MinMilliSeconds = Long.MIN_VALUE / TicksPerMillisecond;

    public static Datespan Zero = new Datespan(0);
    public static Datespan MaxValue = new Datespan(Long.MAX_VALUE);
    public static Datespan MinValue = new Datespan(Long.MIN_VALUE);

    private long ticks = 0;

    public Datespan() {
        ticks = 0;
    }

    public Datespan(long ticks) {
        this.ticks = ticks;
    }

    public Datespan(int hours, int minutes, int seconds) {
        ticks = timeToTicks(hours, minutes, seconds);
    }

    public Datespan(int days, int hours, int minutes, int seconds, int milliseconds) {
        long totalMilliSeconds = ((long)days * 3600 * 24 + hours * 3600 + minutes * 60 + seconds) * 1000 + milliseconds;
        ticks = totalMilliSeconds * TicksPerMillisecond;
    }

    public long ticks() {
        return ticks;
    }

    public int days() {
        return (int)(ticks / TicksPerDay);
    }

    public int hours() {
        return (int)(ticks / TicksPerHour % 24);
    }

    public int minutes() {
        return (int)(ticks / TicksPerMinute % 60);
    }

    public int seconds() {
        return (int)(ticks / TicksPerSecond % 60);
    }

    public int milliseconds() {
        return (int)(ticks / TicksPerMillisecond) % 1000;
    }

    public double totalDays() {
        return (double)ticks / (double)TicksPerDay;
    }

    public double totalHours() {
        return (double)ticks / (double)TicksPerHour;
    }

    public double totalMinutes() {
        return (double)ticks / (double)TicksPerMinute;
    }

    public double totalSeconds() {
        return (double)ticks / (double)TicksPerSecond;
    }

    public double totalMilliseconds() {
        double temp = (double)ticks / (double)TicksPerMillisecond;
        if(temp > MaxMilliSeconds)
            return MaxMilliSeconds;
        if(temp < MinMilliSeconds)
            return MinMilliSeconds;
        return temp;
    }

    public Datespan duration() {
        return new Datespan(ticks >= 0 ? ticks : -ticks);
    }

    @Override
    public String toString() {
        return "" + days() + " " + hours() + ":" + minutes() + ":" + seconds();
    }

    public Datespan add(Datespan d) {
        ticks += d.ticks;
        return this;
    }

    public Datespan sub(Datespan d) {
        ticks -= d.ticks;
        return this;
    }

    public boolean equal(Datespan t) {
        return ticks == t.ticks;
    }

    public int compare(Datespan t) {
        return ticks < t.ticks ? -1 : ticks == t.ticks ? 0 : 1;
    }

    public static long timeToTicks(int hour, int minute, int second) {
        long totalSeconds = (long)hour * 3600 + (long)minute * 60 + second;
        return totalSeconds * TicksPerSecond;
    }

    public static Datespan fromDays(double value) {
        return interval(value, MillisPerDay);
    }

    public static Datespan fromHours(double value) {
        return interval(value, MillisPerHour);
    }

    public static Datespan fromMinutes(double value) {
        return interval(value, MillisPerMinute);
    }

    public static Datespan fromSeconds(double value) {
        return interval(value, MillisPerSecond);
    }

    public static Datespan fromMilliseconds(double value) {
        return interval(value, 1);
    }

    public static Datespan fromTicks(long value) {
        return new Datespan(value);
    }

    public static Datespan interval(double value, int scale) {
        double tmp = value * scale;
        double millis = tmp + (value >= 0 ? 0.5 : -0.5);
        return new Datespan((long)millis * TicksPerMillisecond);
    }
}
