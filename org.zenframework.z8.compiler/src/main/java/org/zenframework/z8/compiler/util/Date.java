package org.zenframework.z8.compiler.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Date {
    static long TicksPerMillisecond = 10000;
    static long TicksPerSecond = TicksPerMillisecond * 1000;
    static long TicksPerMinute = TicksPerSecond * 60;
    static long TicksPerHour = TicksPerMinute * 60;
    static long TicksPerDay = TicksPerHour * 24;

    static int MillisPerSecond = 1000;
    static int MillisPerMinute = MillisPerSecond * 60;
    static int MillisPerHour = MillisPerMinute * 60;
    static int MillisPerDay = MillisPerHour * 24;

    static int DaysPerYear = 365;
    static int DaysPer4Years = DaysPerYear * 4 + 1;
    static int DaysPer100Years = DaysPer4Years * 25 - 1;
    static int DaysPer400Years = DaysPer100Years * 4 + 1;

    static int DaysTo1601 = DaysPer400Years * 4;
    static int DaysTo1899 = DaysPer400Years * 4 + DaysPer100Years * 3 - 367;
    static int DaysTo10000 = DaysPer400Years * 25 - 366;

    static long MinTicks = 0;
    static long MaxTicks = DaysTo10000 * TicksPerDay - 1;
    static long MaxMillis = (long)DaysTo10000 * MillisPerDay;

    static long FileTimeOffset = DaysTo1601 * TicksPerDay;
    static long DoubleDateOffset = DaysTo1899 * TicksPerDay;
    static long OADateMinAsTicks = (DaysPer100Years - DaysPerYear) * TicksPerDay;
    static double OADateMinAsDouble = -657435;
    static double OADateMaxAsDouble = 2958466;

    static int[] DaysToMonth365 = { 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365 };
    static int[] DaysToMonth366 = { 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366 };

    private long ticks;

    public static Date MinValue = new Date(Date.MinTicks);
    public static Date MaxValue = new Date(Date.MaxTicks);
    public static Date ReasonableMaxValue = new Date(4712, 12, 31, 23, 59, 59);

    enum DatePart {
        Year, DayOfYear, Month, Day
    }

    public Date() {
        this.ticks = dateToTicks(1899, 12, 30);
    }

    public Date(long ticks) {
        this.ticks = ticks;
    }

    public Date(int year, int month, int day) {
        ticks = dateToTicks(year, month, day);
    }

    public Date(int year, int month, int day, int hour, int minute, int second) {
        ticks = dateToTicks(year, month, day) + timeToTicks(hour, minute, second);
    }

    public Date(int year, int month, int day, int hour, int minute, int second, int millisecond) {
        ticks = dateToTicks(year, month, day) + timeToTicks(hour, minute, second) + millisecond * TicksPerMillisecond;
    }

    public Date addYears(int value) {
        return addMonths(value * 12);
    }

    public Date addMonths(int months) {
        int y = getDatePart(DatePart.Year);
        int m = getDatePart(DatePart.Month);
        int d = getDatePart(DatePart.Day);
        int i = m - 1 + months;
        if(i >= 0) {
            m = i % 12 + 1;
            y += i / 12;
        }
        else {
            m = 12 + (i + 1) % 12;
            y += (i - 11) / 12;
        }
        int days = daysInMonth(y, m);
        if(d > days)
            d = days;
        return new Date(dateToTicks(y, m, d) + ticks % TicksPerDay);
    }

    public Date addDays(double value) {
        return add(value, MillisPerDay);
    }

    public Date addHours(double value) {
        return add(value, MillisPerHour);
    }

    public Date addMinutes(double value) {
        return add(value, MillisPerMinute);
    }

    public Date addSeconds(double value) {
        return add(value, MillisPerSecond);
    }

    public Date addMilliseconds(double value) {
        return add(value, 1);
    }

    public Date addTicks(long value) {
        return new Date(ticks + value);
    }

    public static int daysInMonth(int year, int month) {
        if(isLeapYear(year))
            return DaysToMonth366[month] - DaysToMonth366[month - 1];

        return DaysToMonth365[month] - DaysToMonth365[month - 1];
    }

    public Date dateNoTime() {
        return new Date(ticks - ticks % TicksPerDay);
    }

    public Datespan timeOfDay() {
        return new Datespan(ticks % TicksPerDay);
    }

    public int year() {
        return getDatePart(DatePart.Year);
    }

    public int dayOfYear() {
        return getDatePart(DatePart.DayOfYear);
    }

    public int month() {
        return getDatePart(DatePart.Month);
    }

    public int day() {
        return getDatePart(DatePart.Day);
    }

    public int dayOfWeek() {
        return (int)((ticks / TicksPerDay + 1) % 7);
    }

    public int hour() {
        return (int)(ticks / TicksPerHour % 24);
    }

    public int minute() {
        return (int)(ticks / TicksPerMinute % 60);
    }

    public int second() {
        return (int)(ticks / TicksPerSecond % 60);
    }

    public int millisecond() {
        return (int)(ticks / TicksPerMillisecond % 1000);
    }

    public long ticks() {
        return ticks;
    }

    public static Date now() {
        GregorianCalendar c = new GregorianCalendar();

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        int millis = c.get(Calendar.MILLISECOND);

        return new Date(year, month, day, hour, minute, second, millis);
    }

    public static Date today() {
        long ticks = now().ticks();
        return new Date(ticks - ticks % TicksPerDay);
    }

    public static boolean isLeapYear(int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
    }

    @Override
    public String toString() {
        return "" + day() + '/' + month() + '/' + year();
    }

    public static Date fromOADate(double d) {
        return new Date(doubleDateToTicks(d));
    }

    public double toOADate() {
        return ticksToOADate(ticks);
    }

    private Date add(double value, int scale) {
        long millis = (long)(value * scale + (value >= 0 ? 0.5 : -0.5));
        return new Date(ticks + millis * TicksPerMillisecond);
    }

    private static long dateToTicks(int year, int month, int day) {
        int daysInMonth = isLeapYear(year) ? DaysToMonth366[month - 1] : DaysToMonth365[month - 1];

        int y = year - 1;
        int n = y * 365 + y / 4 - y / 100 + y / 400 + daysInMonth + day - 1;
        return n * TicksPerDay;
    }

    private static long timeToTicks(int hour, int minute, int second) {
        return Datespan.timeToTicks(hour, minute, second);
    }

    private static long doubleDateToTicks(double value) {
        long millis = (long)(value * MillisPerDay + (value >= 0 ? 0.5 : -0.5));

        if(millis < 0)
            millis -= (millis % MillisPerDay) * 2;

        millis += DoubleDateOffset / TicksPerMillisecond;
        return millis * TicksPerMillisecond;
    }

    private int getDatePart(DatePart part) {
        int n = (int)(ticks / TicksPerDay);
        int y400 = n / DaysPer400Years;
        n -= y400 * DaysPer400Years;
        int y100 = n / DaysPer100Years;
        if(y100 == 4)
            y100 = 3;
        n -= y100 * DaysPer100Years;
        int y4 = n / DaysPer4Years;
        n -= y4 * DaysPer4Years;
        int y1 = n / DaysPerYear;
        if(y1 == 4)
            y1 = 3;
        if(part == DatePart.Year)
            return y400 * 400 + y100 * 100 + y4 * 4 + y1 + 1;

        n -= y1 * DaysPerYear;
        if(part == DatePart.DayOfYear)
            return n + 1;

        boolean leapYear = y1 == 3 && (y4 != 24 || y100 == 3);
        int[] days = leapYear ? DaysToMonth366 : DaysToMonth365;
        int m = (n >> 5) + 1;
        while(n >= days[m])
            ++m;

        if(part == DatePart.Month)
            return m;

        return n - days[m - 1] + 1;
    }

    private static double ticksToOADate(long value) {
        if(value == 0)
            return 0;
        if(value < TicksPerDay)
            value += DoubleDateOffset;

        long millis = (value - DoubleDateOffset) / TicksPerMillisecond;
        if(millis < 0) {
            long frac = millis / MillisPerDay;
            if(frac != 0)
                millis -= (MillisPerDay + frac) * 2;
        }
        return (double)millis / MillisPerDay;
    }

}
