package org.zenframework.z8.server.types;

import java.text.ParseException;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.exceptions.MaskParseException;
import org.zenframework.z8.server.format.DatespanFormat;
import org.zenframework.z8.server.types.sql.sql_datespan;

public final class datespan extends primary {
    private static final long serialVersionUID = -8389890681355639095L;

    private long m_ticks = 0;

    static final public long TicksPerSecond = 1000;
    static final public long TicksPerMinute = TicksPerSecond * 60;
    static final public long TicksPerHour = TicksPerMinute * 60;
    static final public long TicksPerDay = TicksPerHour * 24;

    static final public String defaultMask = "dd HH:mm:ss";

    public datespan() {}

    public datespan(long ticks) {
        set(ticks);
    }

    public datespan(integer ticks) {
        set(ticks.get());
    }

    public datespan(datespan dateSpan) {
        set(dateSpan.m_ticks);
    }

    public datespan(String s) {
        try {
            set(new DatespanFormat(defaultMask).parse(s));
        }
        catch(ParseException e) {
            throw new MaskParseException(new string(s), defaultMask);
        }
    }

    public datespan(int days, int hours, int minutes, int seconds) {
        this(days, hours, minutes, seconds, 0);
    }

    public datespan(int days, int hours, int minutes, int seconds, long milliseconds) {
        set(days, hours, minutes, seconds, milliseconds);
    }

    @Override
    public datespan defaultValue() {
        return new datespan();
    }

    public long get() {
        return m_ticks;
    }

    public void set(datespan span) {
        set(span.m_ticks);
    }

    public void set(int Day, int Hour, int Minute, int Second, long Millisecond) {
        set(Day * TicksPerDay + Hour * TicksPerHour + Minute * TicksPerMinute + Second * TicksPerSecond + Millisecond);
    }

    public void set(long ticks) {
        m_ticks = ticks;
    }

    public long days() {
        return m_ticks / TicksPerDay;
    }

    public long hours() {
        return m_ticks / TicksPerHour % 24;
    }

    public long minutes() {
        return m_ticks / TicksPerMinute % 60;
    }

    public long Seconds() {
        return m_ticks / TicksPerSecond % 60;
    }

    public long milliseconds() {
        return m_ticks;
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
        return new integer(Seconds());
    }

    public integer z8_milliseconds() {
        return new integer(milliseconds());
    }

    public integer z8_totalHours() {
        return new integer(m_ticks / TicksPerHour);
    }

    public integer z8_totalMinutes() {
        return new integer(m_ticks / TicksPerMinute);
    }

    public integer z8_totalSeconds() {
        return new integer(m_ticks / TicksPerSecond);
    }

    public void z8_setTotalHours(integer x) {
        m_ticks = x.get() * TicksPerHour;
    }

    public void z8_setTotalMinutes(integer x) {
        m_ticks = x.get() * TicksPerMinute;
    }

    public void z8_setTotalSeconds(integer x) {
        m_ticks = x.get() * TicksPerSecond;
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
    public String toDbString(DatabaseVendor dbtype) {
        return toDbConstant(dbtype);
    }

    @Override
    public int hashCode() {
        return new Long(m_ticks).hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof datespan) {
            datespan datespan = (datespan)object;
            return m_ticks == datespan.m_ticks;
        }
        return false;
    }

    public String format(String frm) {
        return new DatespanFormat(frm).format(this).toString();
    }

    @Override
    public String toString() {
        return format(defaultMask);
    }

    public string z8_toString(string frm) {
        return new string(format(frm.get()));
    }

    public sql_datespan sql_datespan() {
        return new sql_datespan(this);
    }

    public void operatorAssign(datespan value) {
        set(value);
    }

    public datetime operatorAdd(date x) {
        return operatorAdd(x.datetime());
    }

    public datetime operatorAdd(datetime x) {
        return new datetime(x.get().getTimeInMillis() + milliseconds());
    }

    public datespan operatorAdd(datespan x) {
        return new datespan(milliseconds() + x.milliseconds());
    }

    public datespan operatorSub(datespan x) {
        return new datespan(milliseconds() - x.milliseconds());
    }

    public datespan operatorAddAssign(datespan x) {
        set(operatorAdd(x));
        return this;
    }

    public datespan operatorSubAssign(datespan x) {
        set(operatorSub(x));
        return this;
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
        return new datespan(string.get());
    }
}
