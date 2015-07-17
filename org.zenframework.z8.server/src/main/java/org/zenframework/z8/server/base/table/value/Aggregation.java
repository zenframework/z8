package org.zenframework.z8.server.base.table.value;

public enum Aggregation {
    
    None(Names.None),
    Sum(Names.Sum),
    Min(Names.Min),
    Max(Names.Max),
    Average(Names.Average),
    Count(Names.Count),
    Array(Names.Array),
    Concat(Names.Concat);

    class Names {
        static protected final String None = "none";
        static protected final String Sum = "sum";
        static protected final String Min = "min";
        static protected final String Max = "max";
        static protected final String Average = "average";
        static protected final String Count = "count";
        static protected final String Array = "array";
        static protected final String Concat = "concat";
    }

    private String fName = null;

    Aggregation(String name) {
        fName = name;
    }

    @Override
    public String toString() {
        return fName;
    }

}
