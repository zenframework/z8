package org.zenframework.z8.server.db.sql;

public enum SortDirection {
    Asc(Names.Asc),
    Desc(Names.Desc);

    class Names {
        static protected final String Asc = "Asc";
        static protected final String Desc = "Desc";
    }

    private String fName = null;

    SortDirection(String name) {
        fName = name;
    }

    @Override
    public String toString() {
        return fName;
    }

    static public SortDirection fromString(String string) {
        if(Names.Asc.equalsIgnoreCase(string)) {
            return SortDirection.Asc;
        }
        else if(Names.Desc.equalsIgnoreCase(string)) {
            return SortDirection.Desc;
        }
        else {
            throw new RuntimeException("Unknown sort direction: '" + string + "'");
        }
    }

}
