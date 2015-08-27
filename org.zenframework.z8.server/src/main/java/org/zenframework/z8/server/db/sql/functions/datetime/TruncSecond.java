package org.zenframework.z8.server.db.sql.functions.datetime;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Mul;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.functions.conversion.ToDatetime;
import org.zenframework.z8.server.exceptions.UnsupportedParameterException;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.integer;

public class TruncSecond extends SqlToken {
    private SqlToken param1;

    public TruncSecond(SqlToken p1) {
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
                return new ToDatetime(param1).format(vendor, options);
            case Postgres:
                return "date_trunc('second', " + param1.format(vendor, options) + ")";
            case SqlServer:
                return "Convert(datetime, convert(varchar(19)," + param1.format(vendor, options) + ", 120), 120)";
            default:
                throw new UnknownDatabaseException();
            }

        case Datespan:
            return new Mul(new TotalMinute(param1), Operation.Mul, new SqlConst(new integer(datespan.TicksPerMinute)))
                    .format(vendor, options);

        default:
            throw new UnsupportedParameterException();
        }
    }

    @Override
    public FieldType type() {
        return param1.type();
    }
}
