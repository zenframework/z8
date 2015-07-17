package org.zenframework.z8.server.db.sql.functions.datetime;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.UnsupportedParameterException;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.datespan;

public class AddMinute extends SqlToken {
    private SqlToken param1;
    private SqlToken param2;

    public AddMinute(SqlToken p1, SqlToken p2) {
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
        switch(param1.type()) {
        case Date:
        case Datetime:
            switch(vendor) {
            case Oracle:
                return "(" + param1.format(vendor, options) + "+(" + param2.format(vendor, options) + ")/(24*60))";
            case Postgres:
                return "(" + param1.format(vendor, options) + " + (" + param2.format(vendor, options) + ") * interval '1 munute')";
            case SqlServer:
                return "DATEADD(mi, " + param2.format(vendor, options) + ", " + param1.format(vendor, options) + ")";
            default:
                throw new UnknownDatabaseException();
            }

        case Datespan:
            return param1.format(vendor, options) + "+(" + param2.format(vendor, options) + "*" + datespan.TicksPerMinute
                    + ")";

        default:
            throw new UnsupportedParameterException();
        }
    }

    @Override
    public FieldType type() {
        return param1.type();
    }

    @Override
    public String formula() {
        return param1.formula() + ".add(Date.MINUTE, " + param2.formula() + ")";
    }
}
