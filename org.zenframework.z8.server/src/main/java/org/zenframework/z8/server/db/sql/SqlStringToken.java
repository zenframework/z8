package org.zenframework.z8.server.db.sql;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class SqlStringToken extends SqlToken {
    private String value;

    public SqlStringToken(String value) {
        this.value = value;
    }

    @Override
    public void collectFields(Collection<IValue> fields) {}

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext)
            throws UnknownDatabaseException {
        return value;
    }

    @Override
    public FieldType type() {
        return FieldType.Null;
    }
}
