package org.zenframework.z8.server.types;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;

public class math extends OBJECT {
    public static class CLASS<T extends math> extends OBJECT.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(math.class);
            setAttribute(Native, math.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new math(container);
        }
    }

    public math(IObject container) {
        super(container);
    }

    static public integer z8_max(integer a, integer b) {
        return new integer(a.operatorMoreEqu(b).get() ? a : b);
    }

    static public decimal z8_max(integer a, decimal b) {
        return new decimal(a.operatorMoreEqu(b).get() ? new decimal(a) : b);
    }

    static public decimal z8_max(decimal a, integer b) {
        return new decimal(a.operatorMoreEqu(b).get() ? a : new decimal(b));
    }

    static public decimal z8_max(decimal a, decimal b) {
        return new decimal(a.operatorMoreEqu(b).get() ? a : b);
    }

    static public integer z8_min(integer a, integer b) {
        return new integer(a.operatorLessEqu(b).get() ? a : b);
    }

    static public decimal z8_min(integer a, decimal b) {
        return new decimal(a.operatorLessEqu(b).get() ? new decimal(a) : b);
    }

    static public decimal z8_min(decimal a, integer b) {
        return new decimal(a.operatorLessEqu(b).get() ? a : new decimal(b));
    }

    static public decimal z8_min(decimal a, decimal b) {
        return new decimal(a.operatorLessEqu(b).get() ? a : b);
    }

    static public date z8_max(date a, date b) {
        return new date(a.operatorMoreEqu(b).get() ? a : b);
    }

    static public datetime z8_max(datetime a, date b) {
        return new datetime(a.operatorMoreEqu(b).get() ? a : new datetime(b));
    }

    static public datetime z8_max(date a, datetime b) {
        return new datetime(a.operatorMoreEqu(b).get() ? new datetime(a) : b);
    }

    static public datetime z8_max(datetime a, datetime b) {
        return new datetime(a.operatorMoreEqu(b).get() ? a : b);
    }

    static public datespan z8_max(datespan a, datespan b) {
        return new datespan(a.operatorMoreEqu(b).get() ? a : b);
    }

    static public string z8_max(string a, string b) {
        return new string(a.operatorMoreEqu(b).get() ? a : b);
    }

    static public date z8_min(date a, date b) {
        return new date(a.operatorLessEqu(b).get() ? a : b);
    }

    static public datetime z8_min(datetime a, date b) {
        return new datetime(a.operatorLessEqu(b).get() ? a : new datetime(b));
    }

    static public datetime z8_min(date a, datetime b) {
        return new datetime(a.operatorLessEqu(b).get() ? new datetime(a) : b);
    }

    static public datetime z8_min(datetime a, datetime b) {
        return new datetime(a.operatorLessEqu(b).get() ? a : b);
    }

    static public datespan z8_min(datespan a, datespan b) {
        return new datespan(a.operatorLessEqu(b).get() ? a : b);
    }

    static public string z8_min(string a, string b) {
        return new string(a.operatorLessEqu(b).get() ? a : b);
    }

    static public decimal z8_hypot(decimal x, decimal y) {
        return new decimal(Math.hypot(x.get().doubleValue(), y.get().doubleValue()));
    }

    static public decimal z8_random() {
        return new decimal(Math.random());
    }
}
