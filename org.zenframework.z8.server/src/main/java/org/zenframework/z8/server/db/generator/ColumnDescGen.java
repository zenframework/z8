package org.zenframework.z8.server.db.generator;

public class ColumnDescGen extends Column {
    public boolean DescExist;

    public ColumnDescGen(String name, String type, int size, int scale, boolean nullable, String defaultValue) {
        super(name, type, size, scale, nullable, defaultValue);
        DescExist = false;
    }

    public ColumnDescGen(Column desc) {
        this(desc.name, desc.type, desc.size, desc.scale, desc.nullable, desc.defaultValue);
    }
}
