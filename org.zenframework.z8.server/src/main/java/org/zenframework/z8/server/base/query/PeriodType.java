package org.zenframework.z8.server.base.query;

public enum PeriodType {
    Year(Names.Year),
    HalfYear(Names.HalfYear),
    Quarter(Names.Quarter),
    Month(Names.Month),
    Week(Names.Week),
    Default(Names.Default);

    class Names {
        static protected final String Year = "year";
        static protected final String HalfYear = "halfYear";
        static protected final String Quarter = "quarter";
        static protected final String Month = "month";
        static protected final String Week = "week";
        static protected final String Default = "default";
    }

    private String fName = null;

    PeriodType(String name) {
        fName = name;
    }

    @Override
    public String toString() {
        return fName;
    }

    static public PeriodType fromString(String string) {
        if(Names.Year.equals(string)) {
            return PeriodType.Year;
        }
        else if(Names.HalfYear.equals(string)) {
            return PeriodType.HalfYear;
        }
        else if(Names.Quarter.equals(string)) {
            return PeriodType.Quarter;
        }
        else if(Names.Month.equals(string)) {
            return PeriodType.Month;
        }
        else if(Names.Week.equals(string)) {
            return PeriodType.Week;
        }
        else if(Names.Default.equals(string)) {
            return PeriodType.Default;
        }
        else {
            throw new RuntimeException("Unknown period type: '" + string + "'");
        }
    }
}
