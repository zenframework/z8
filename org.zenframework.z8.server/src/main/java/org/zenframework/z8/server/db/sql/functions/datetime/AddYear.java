package org.zenframework.z8.server.db.sql.functions.datetime;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class AddYear extends SqlToken {
    private SqlToken param1;
    private SqlToken param2;

    public AddYear(SqlToken p1, SqlToken p2) {
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
        switch(vendor) {
        case Oracle:
            return "ADD_MONTHS(" + param1.format(vendor, options) + ",(" + param2.format(vendor, options) + ")*12)";
        case Postgres:
            return "(" + param1.format(vendor, options) + " + (" + param2.format(vendor, options) + ") * interval '1 year')";
        case SqlServer:
            return "DATEADD(YEAR, " + param2.format(vendor, options) + ", " + param1.format(vendor, options) + ")";
        default:
            throw new UnknownDatabaseException();
        }
    }

    @Override
    public FieldType type() {
        return param1.type();
    }

    @Override
    public String formula() {
        return param1.formula() + ".add(Date.YEAR, " + param2.formula() + ")";
    }
}
