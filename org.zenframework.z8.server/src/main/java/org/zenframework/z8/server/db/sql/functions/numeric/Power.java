package org.zenframework.z8.server.db.sql.functions.numeric;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;

public class Power extends SqlToken {
    private SqlToken param1;
    private SqlToken param2;

    public Power(SqlToken p1, SqlToken p2) {
        param1 = p1;
        param2 = p2;
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        param1.collectFields(fields);
        param2.collectFields(fields);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
        return "POWER(" + param1.format(vendor, options) + ", " + param2.format(vendor, options) + ")";
    }

    @Override
    public FieldType type() {
        return param1.type();
    }

    @Override
    public String formula() {
        return "Math.pow(" + param1.formula() + ", " + param2.formula() + ")";
    }
}
