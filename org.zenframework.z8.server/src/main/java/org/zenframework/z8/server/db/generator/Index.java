package org.zenframework.z8.server.db.generator;

import java.util.ArrayList;
import java.util.List;

public class Index {
    public String name;
    public String tableName;
    public List<String> fields = new ArrayList<String>();
    public boolean unique;

    public Index(String name, String tableName, String fieldName, boolean unique) {
        this.name = name;
        this.tableName = tableName;
        this.fields.add(fieldName);
        this.unique = unique;
    }

    @Override
    public String toString() {
        String s = name + " " + (unique ? "UNIQUE" : "NONUNIQUE");

        for(String col : fields) {
            s += " " + col;
        }

        return s;
    }
}
