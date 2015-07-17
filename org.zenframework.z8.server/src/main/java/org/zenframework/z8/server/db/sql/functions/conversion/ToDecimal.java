package org.zenframework.z8.server.db.sql.functions.conversion;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class ToDecimal extends SqlToken {
    private SqlToken param1;

    public ToDecimal(SqlToken p1) {
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
            return "TO_NUMBER(" + param1.format(vendor, options) + ", '999999999999999999999999.9999999999')";
        case SqlServer:
            return "CONVERT(numeric(36,14)," + param1.format(vendor, options) + ")";
        case Postgres:
            //return "FLOAT(" + param1.format(vendor, options) + ")";
            return "CAST(" + param1.format(vendor, options) + " AS FLOAT)";
        default:
            throw new UnknownDatabaseException();
        }
    }

    @Override
    public FieldType type() {
        return FieldType.Decimal;
    }
}
