package org.zenframework.z8.compiler.util;

public class Datetime extends Date {
    public Datetime() {
        super();
    }

    public Datetime(long ticks) {
        super(ticks);
    }

    public Datetime(Date date) {
        super(date.year(), date.month(), date.day());
    }

    public Datetime(int year, int month, int day) {
        super(year, month, day);
    }

    public Datetime(int year, int month, int day, int hour, int minute, int second) {
        super(year, month, day, hour, minute, second);
    }

    public Datetime(int year, int month, int day, int hour, int minute, int second, int millisecond) {
        super(year, month, day, hour, minute, second, millisecond);
    }

    @Override
    public String toString() {
        return super.toString() + ' ' + hour() + ':' + minute() + ':' + second();
    }
}
