package org.zenframework.z8.server.db.generator;

import java.util.ArrayList;
import java.util.Collection;

public class PrimaryKey {
    public String name;
    public String tableName;
    public Collection<String> fields = new ArrayList<String>();

    PrimaryKey(String name, String tableName, String fieldName) {
        this.name = name;
        this.tableName = tableName;
        this.fields.add(fieldName);
    }

    @Override
    public String toString() {
        String s = name;
        for(String col : fields) {
            s += " " + col;
        }
        return s;
    }
}
