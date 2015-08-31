package org.zenframework.z8.server.base.query;

public enum ChartType {
    Bar(Names.Bar),
    Column(Names.Column),
    Line(Names.Line),
    Pie(Names.Pie);

    class Names {
        static protected final String Bar = "BarChart";
        static protected final String Column = "ColumnChart";
        static protected final String Line = "LineChart";
        static protected final String Pie = "PieChart";
    }

    private String fName = null;

    ChartType(String name) {
        fName = name;
    }

    @Override
    public String toString() {
        return fName;
    }

    static public ChartType fromString(String string) {
        if(Names.Bar.equals(string)) {
            return ChartType.Bar;
        }
        else if(Names.Column.equals(string)) {
            return ChartType.Column;
        }
        else if(Names.Line.equals(string)) {
            return ChartType.Line;
        }
        else if(Names.Pie.equals(string)) {
            return ChartType.Pie;
        }
        else {
            throw new RuntimeException("Unknown chart type: '" + string + "'");
        }
    }
}
