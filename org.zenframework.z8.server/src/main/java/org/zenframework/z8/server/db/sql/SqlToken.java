package org.zenframework.z8.server.db.sql;

import java.util.Collection;
import java.util.HashSet;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;

abstract public class SqlToken {
    public String format(DatabaseVendor vendor, FormatOptions options) {
        return format(vendor, options, false);
    }

    abstract public void collectFields(Collection<IValue> fields);

    abstract public String format(DatabaseVendor vendor, FormatOptions options, boolean isLogicalContext);

    public String formula() {
        assert (false);
        return null;
    }

    public Collection<IValue> getUsedFields() {
        Collection<IValue> result = new HashSet<IValue>();
        collectFields(result);
        return result;
    }

    public boolean isNumeric() {
        return isInteger() || isDecimal();
    }

    public boolean isInteger() {
        return type() == FieldType.Integer;
    }

    public boolean isDecimal() {
        return type() == FieldType.Decimal;
    }

    public boolean isDate() {
        FieldType type = type();
        return type == FieldType.Date || type == FieldType.Datetime;
    }

    public boolean isDatespan() {
        return type() == FieldType.Datespan;
    }

    abstract public FieldType type();
}
