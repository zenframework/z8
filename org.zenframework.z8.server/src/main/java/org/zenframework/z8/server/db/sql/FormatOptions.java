package org.zenframework.z8.server.db.sql;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.table.value.IValue;

public class FormatOptions {
    private Map<IValue, String> aliases = new HashMap<IValue, String>();

    public String getFieldAlias(IValue field) {
        return aliases.get(field);
    }

    public void setFieldAlias(IValue field, String alias) {
        aliases.put(field, alias);
    }
}
