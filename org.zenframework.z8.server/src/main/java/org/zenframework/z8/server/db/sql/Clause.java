package org.zenframework.z8.server.db.sql;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.exceptions.UnsupportedParameterException;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Clause extends SqlToken {
    private LinkedHashMap<String, SqlToken> items = new LinkedHashMap<String, SqlToken>();
    protected Clause container;
    private String delimiter;

    public Clause() {
        this("");
    }

    public Clause(String delimiter) {
        this.delimiter = delimiter;
        container = this;
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        for(SqlToken item : items.values()) {
            item.collectFields(fields);
        }
    }

    public void add(SqlToken item) {
        add(item, item.toString());
    }

    public void add(SqlToken item, String name) {
        if(container == this) {
            items.put(name, item);
        }
        else {
            container.add(item, name);
        }
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext)
            throws UnknownDatabaseException {
        Iterator<SqlToken> iter = iterator();
        String ret = "";
        if(!container.isEmpty()) {
            while(iter.hasNext()) {
                ret += iter.next().format(vendor, options, logicalContext);
                if(iter.hasNext())
                    ret += delimiter;
            }
        }
        return ret;
    }

    @Override
    public FieldType type() {
        throw new UnsupportedParameterException();
    }

    public SqlToken getItem(String name) {
        return items.get(name);
    }

    public Clause getClause(String name) {
        return (Clause)getItem(name);
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public void clear() {
        if(container == this)
            items.clear();
        else
            container.clear();
    }

    public void addAll(Clause clause) {
        Iterator<SqlToken> iter = clause.container.iterator();

        while(iter.hasNext()) {
            add(iter.next());
        }
    }

    public int size() {
        return items.size();
    }

    protected Iterator<SqlToken> iterator() {
        return items.values().iterator();
    }
}
