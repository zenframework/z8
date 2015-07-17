package org.zenframework.z8.server.db;

public enum DatabaseVendor {
    Oracle(names.Oracle),
    SqlServer(names.SqlServer),
    Postgres(names.Postgres);

    class names {
        static protected final String Oracle = "Oracle";
        static protected final String SqlServer = "SqlServer";
        static protected final String Postgres = "Postgres";
    }

    private String fName = null;

    DatabaseVendor(String name) {
        fName = name;
    }

    @Override
    public String toString() {
        return fName;
    }

    public static DatabaseVendor fromString(String string) {
        if(string == null) {
            return DatabaseVendor.SqlServer;
        }

        string = string.toUpperCase();

        if(string.contains(names.Oracle.toUpperCase())) {
            return DatabaseVendor.Oracle;
        }
        else if(string.contains(names.SqlServer.toUpperCase())) {
            return DatabaseVendor.SqlServer;
        }
        else if(string.contains(names.Postgres.toUpperCase())) {
            return DatabaseVendor.Postgres;
        }
        else {
            return DatabaseVendor.SqlServer;
        }
    }

    public String quote(String name) {
        return quoteOpen() + name + quoteClose();
    }

    private char quoteOpen() {
        return this == Oracle || this == Postgres ? '"' : '[';
    }

    private char quoteClose() {
        return this == Oracle || this == Postgres ? '"' : ']';
    }
}
