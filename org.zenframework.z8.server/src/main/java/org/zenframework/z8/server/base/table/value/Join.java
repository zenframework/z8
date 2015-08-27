package org.zenframework.z8.server.base.table.value;

public enum Join {
    Inner(Names.Inner),
    Left(Names.Left),
    Right(Names.Right),
    Full(Names.Full);

    class Names {
        static protected final String Inner = "inner";
        static protected final String Left = "left";
        static protected final String Right = "right";
        static protected final String Full = "full";
    }

    private String fName = null;

    Join(String name) {
        fName = name;
    }

    @Override
    public String toString() {
        return fName;
    }

    static public Join fromString(String string) {
        if(Names.Inner.equals(string)) {
            return Join.Inner;
        } else if(Names.Left.equals(string)) {
            return Join.Left;
        } else if(Names.Right.equals(string)) {
            return Join.Right;
        } else if(Names.Full.equals(string)) {
            return Join.Full;
        } else {
            throw new RuntimeException("Unknown join type: '" + string + "'");
        }
    }
}
