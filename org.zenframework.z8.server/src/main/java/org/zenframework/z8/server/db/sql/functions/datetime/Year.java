package org.zenframework.z8.server.db.sql.functions.datetime;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.conversion.ToNumber;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Year extends SqlToken {
    private SqlToken param1;

    public Year(SqlToken p1) {
        param1 = p1;
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        param1.collectFields(fields);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
        switch(vendor) {
        case Postgres:
            return "cast(date_part('year', " + param1.format(vendor, options) + ") as bigint)";
        case Oracle:
            return new ToNumber(new SqlStringToken("TO_CHAR(" + param1.format(vendor, options) + ", 'YYYY')")).format(
                    vendor, options);
        case SqlServer:
            return "Year(" + param1.format(vendor, options) + ")";
        default:
            throw new UnknownDatabaseException();
        }
    }

    @Override
    public FieldType type() {
        return FieldType.Integer;
    }

    @Override
    public String formula() {
        return param1.formula() + ".getFullYear()";
    }
}
