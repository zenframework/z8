package org.zenframework.z8.server.db.sql.functions.datetime;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.conversion.ToNumber;
import org.zenframework.z8.server.exceptions.UnsupportedParameterException;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class YearDay extends SqlToken {
    private SqlToken param1;

    public YearDay(SqlToken p1) {
        param1 = p1;
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        param1.collectFields(fields);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
        switch(param1.type()) {
        case Date:
        case Datetime:
            switch(vendor) {
            case Oracle:
                return new ToNumber(new SqlStringToken("TO_CHAR(" + param1.format(vendor, options) + ", 'DDD')")).format(
                        vendor, options);
            case SqlServer:
                return "DATEPART(dayofyear, " + param1.format(vendor, options) + ")";
            default:
                throw new UnknownDatabaseException();
            }

        default:
            throw new UnsupportedParameterException();
        }
    }

    @Override
    public FieldType type() {
        return FieldType.Integer;
    }
}
