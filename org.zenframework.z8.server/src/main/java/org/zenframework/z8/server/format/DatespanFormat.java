package org.zenframework.z8.server.format;

import java.text.*;

import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.datespan;

public class DatespanFormat extends Format {
    private String pattern;

    private static String attrDay = "dd";
    private static String attrHour = "HH";
    private static String attrMinute = "mm";
    private static String attrSecond = "ss";

    public DatespanFormat(String _pattern) {
        pattern = _pattern;
    }

    public StringBuffer format(datespan dt) {
        return format(dt, new StringBuffer(), new FieldPosition(0));
    }

    public StringBuffer format(datespan dt, StringBuffer toAppendTo, FieldPosition pos) {
        String sHours = Long.toString(dt.hours());
        if(dt.hours() < 10)
            sHours = "0" + sHours;
        String sMinutes = Long.toString(dt.minutes());
        if(dt.minutes() < 10)
            sMinutes = "0" + sMinutes;
        String sSeconds = Long.toString(dt.Seconds());
        if(dt.Seconds() < 10)
            sSeconds = "0" + sSeconds;
        return new MessageFormat(GetMessageMask()).format(new Object[] { dt.days(), sHours, sMinutes, sSeconds },
                toAppendTo, pos);
    }

    public datespan parse(String source) throws ParseException {
        return parse(source, new ParsePosition(0));
    }

    public datespan parse(String source, ParsePosition pos) throws ParseException {
        Object[] frm = new MessageFormat(GetMessageMask()).parse(source, pos);
        if(frm == null) {
            throw new ParseException(source, 0);
        }
        int days = 0, hours = 0, minutes = 0, seconds = 0;
        if(pattern.indexOf(attrDay) != -1)
            days = Integer.parseInt((String)frm[0], 10);
        if(pattern.indexOf(attrHour) != -1)
            hours = Integer.parseInt((String)frm[1], 10);
        if(pattern.indexOf(attrMinute) != -1)
            minutes = Integer.parseInt((String)frm[2], 10);
        if(pattern.indexOf(attrSecond) != -1)
            seconds = Integer.parseInt((String)frm[3], 10);
        return new datespan(days, hours, minutes, seconds, 0);
    }

    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        return format((datespan)obj, toAppendTo, pos);
    }

    public Object parseObject(String source, ParsePosition pos) {
        try {
            return parse(source, pos);
        }
        catch(ParseException e) {
            Trace.logError(e);
        }
        return null;
    }

    private String GetMessageMask() {
        String mask = pattern;
        mask = mask.replace(attrDay, "{0}");
        mask = mask.replace(attrHour, "{1}");
        mask = mask.replace(attrMinute, "{2}");
        mask = mask.replace(attrSecond, "{3}");
        return mask;
    }
}
