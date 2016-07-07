package org.zenframework.z8.server.base.query;

import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.string;

public class Period extends OBJECT {
    public static class CLASS<T extends Period> extends OBJECT.CLASS<T> {
        public CLASS() {
            this(null);
        }

        public CLASS(IObject container) {
            super(container);
            setJavaClass(Period.class);
        }

        @Override
        public Object newObject(IObject container) {
            return new Period(container);
        }
    }

    public PeriodType type = PeriodType.Quarter;
    public date start = new date();
    public date finish = new date();

    protected Period(IObject container) {
        super(container);
    }

    static private date start(date date, PeriodType period) {
        int year = date.year();
        int month = date.month();
        int dayOfWeek = date.dayOfWeek() - 1; // sunday == 0

        if(period == PeriodType.Year) {
            return date.truncYear();
        }
        else if(period == PeriodType.HalfYear) {
            return month <= 6 ? new date(year, 1, 1) : new date(year, 7, 1);
        }
        else if(period == PeriodType.Quarter) {
            return date.truncQuarter();
        }
        else if(period == PeriodType.Month) {
            return date.truncMonth();
        }
        else if(period == PeriodType.Week) {
            return date.addDay(dayOfWeek == 0 ? -6 : (1 - dayOfWeek));
        }

        return date;
    }

    static private date finish(date date, PeriodType period) {
        int year = date.year();
        int month = date.month();
        int dayOfWeek = date.dayOfWeek() - 1;

        if(period == PeriodType.Year) {
            return new date(year, 12, 31);
        }
        else if(period == PeriodType.HalfYear) {
            return month <= 6 ? new date(year, 6, 30) : new date(year, 12, 31);
        }
        else if(period == PeriodType.Quarter) {
            if(month <= 3)
                return new date(year, 3, 31);
            else if(month <= 6)
                return new date(year, 6, 30);
            else if(month <= 9)
                return new date(year, 9, 30);
            else
                return new date(year, 12, 31);
        }
        else if(period == PeriodType.Month) {
            return new date(year, month, 1).addMonth(1).addDay(-1);
        }
        else if(period == PeriodType.Week) {
            return date.addDay(dayOfWeek == 0 ? 0 : (6 - dayOfWeek + 1));
        }

        return date;
    }

    static public Period.CLASS<? extends Period> z8_create(PeriodType type) {
        return create(type, start(new date(), type), finish(new date(), type));
    }

    static public Period.CLASS<? extends Period> z8_create(PeriodType type, date start) {
        return create(type, start(start, type), finish(start, type));
    }

    static public Period.CLASS<? extends Period> z8_create(date start, date finish) {
        return create(PeriodType.Default, start, finish);
    }

    static private Period.CLASS<? extends Period> create(PeriodType type, date start, date finish) {
        Period.CLASS<Period> period = new Period.CLASS<Period>();
        period.get().type = type;
        period.get().start.set(start);
        period.get().finish.set(finish);
        return period;
    }

    @Override
    public String toString() {
        return start.toString() + " - " + finish.toString();
    }

    @Override
    public string z8_toString() {
        return string();
    }

    public bool z8_isEmpty() {
        return new bool(start.equals(date.MIN) && finish.equals(date.MIN));
    }

    public Period.CLASS<? extends Period> z8_intersect(Period.CLASS<? extends Period> periodClass) {
        long start = periodClass.get().start.getTicks();
        long finish = periodClass.get().finish.getTicks();

        long myStart = this.start.getTicks();
        long myFinish = this.finish.getTicks();

        date resultStart = new date(date.MIN);
        date resultFinish = new date(date.MIN);

        if(start <= myStart && myStart <= finish) {
            resultStart.set(new date(myStart));
        }

        if(start <= myFinish && myFinish <= finish) {
            resultFinish.set(new date(myFinish));
        }

        if(myStart <= start && start <= myFinish) {
            resultStart.set(new date(start));
        }

        if(myStart <= finish && finish <= myFinish) {
            resultFinish.set(new date(finish));
        }

        return create(PeriodType.Default, resultStart, resultFinish);
    }

    public string string() {
        return new string(toString());
    }

    public static Period.CLASS<? extends Period> parse(String json) {
        JsonObject object = new JsonObject(json);

        PeriodType type = PeriodType.fromString(object.getString(Json.period));
        date start = new date(object.getString(Json.start));
        date finish = new date(object.getString(Json.finish));

        return create(type, start, finish);
    }

    static private String[] monthNames = { "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август",
            "Сентябрь", "Октябрь", "Ноябрь", "Декабрь" };

    @Override
    public String displayName() {
        if(type == PeriodType.Default) {
            return toString();
        }

        int year = start.year();
        int month = start.month();

        String yearText = year + " год";

        if(type == PeriodType.Year) {
            return yearText;
        }

        yearText += ", ";

        if(type == PeriodType.HalfYear) {
            return yearText + (month <= 6 ? '1' : '2') + "-ое полугодие";
        }
        else if(type == PeriodType.Quarter) {
            return yearText + (month <= 3 ? "1-ый" : (month <= 6 ? "2-ой" : (month <= 9 ? "3-ий" : "4-ый"))) + " квартал";
        }
        else if(type == PeriodType.Month) {
            return yearText + monthNames[month - 1];
        }
        else if(type == PeriodType.Week) {
            int week = start.weekOfYear();

            if(month == 11 && week == 1) {
                year += 1;
            }

            return year + " год, " + week + "-я неделя";
        }

        assert (false);
        return "unknown period \'" + type + '\'';
    }
}
