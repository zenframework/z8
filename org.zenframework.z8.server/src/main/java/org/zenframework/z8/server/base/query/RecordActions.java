package org.zenframework.z8.server.base.query;

public enum RecordActions {
    Add(Names.Add),
    Copy(Names.Copy),
    Delete(Names.Delete);

    class Names {
        static protected final String Add = "add";
        static protected final String Copy = "copy";
        static protected final String Delete = "delete";
    }

    private String fName = null;

    RecordActions(String name) {
        fName = name;
    }

    @Override
    public String toString() {
        return fName;
    }

    static public RecordActions fromString(String string) {
        if(Names.Add.equals(string)) {
            return RecordActions.Add;
        }
        else if(Names.Copy.equals(string)) {
            return RecordActions.Copy;
        }
        else if(Names.Delete.equals(string)) {
            return RecordActions.Delete;
        }
        else {
            throw new RuntimeException("Unknown record action: '" + string + "'");
        }
    }
}
