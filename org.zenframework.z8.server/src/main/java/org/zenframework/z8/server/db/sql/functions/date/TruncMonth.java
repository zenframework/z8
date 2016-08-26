package org.zenframework.z8.server.db.sql.functions.date;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class TruncMonth extends SqlToken {
    private SqlToken param1;

    public TruncMonth(SqlToken p1) {
        param1 = p1;
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        param1.collectFields(fields);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
        switch(vendor) {
        case Oracle:
            return "Trunc(" + param1.format(vendor, options) + ", 'MM')";
        case Postgres:
            return "date_trunc('month', " + param1.format(vendor, options) + ")";
        case SqlServer:
            return "Convert(datetime, convert(varchar(8)," + param1.format(vendor, options) + ", 120) + '01', 120)";
        default:
            throw new UnknownDatabaseException();
        }
    }

    @Override
    public FieldType type() {
        return param1.type();
    }
}
