package org.zenframework.z8.server.db.sql;

public class WrappedClause extends Clause {
    public WrappedClause(String delim, String prefix, String suffix) {
        add(new SqlStringToken(prefix), "PREFIX");
        Clause items = new Clause(delim);
        add(items, "CONTAINER");
        add(new SqlStringToken(suffix), "SUFFIX");
        container = items;
    }
}
