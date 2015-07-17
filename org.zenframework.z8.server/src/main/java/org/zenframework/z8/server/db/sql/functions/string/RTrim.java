package org.zenframework.z8.server.db.sql.functions.string;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;

public class RTrim extends StringFunction {
    private SqlToken value;

    public RTrim(Field field) {
        this(new SqlField(field));
    }

    public RTrim(SqlToken value) {
        this.value = value;
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        value.collectFields(fields);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
        return "RTRIM(" + value.format(vendor, options) + ")";
    }
}
