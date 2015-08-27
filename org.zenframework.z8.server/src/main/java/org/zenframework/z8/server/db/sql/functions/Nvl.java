package org.zenframework.z8.server.db.sql.functions;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Nvl extends SqlToken {
    private SqlToken param1;
    private SqlToken param2;

    public Nvl(SqlToken p1, SqlToken p2) {
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
        case Postgres:
            return "nvl(" + param1.format(vendor, options) + "," + param2.format(vendor, options, logicalContext) + ")";
        case SqlServer:
            return "isNull(" + param1.format(vendor, options) + "," + param2.format(vendor, options, logicalContext) + ")";
        default:
            throw new UnknownDatabaseException();
        }
    }

    @Override
    public FieldType type() {
        return param1.type();
    }
}
