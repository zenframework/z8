package org.zenframework.z8.server.base.query;

public enum ReadLock {
    
    None(Names.None),
    Update(Names.Update),
    Share(Names.Share);

    class Names {
        static protected final String None = "";
        static protected final String Share = "for share";
        static protected final String Update = "for update";
    }

    private String fName = null;

    ReadLock(String name) {
        fName = name;
    }

    @Override
    public String toString() {
        return fName;
    }
}
