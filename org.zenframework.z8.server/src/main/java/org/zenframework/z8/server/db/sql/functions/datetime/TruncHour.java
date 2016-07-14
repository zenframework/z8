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
import org.zenframework.z8.server.exceptions.UnsupportedException;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.integer;

public class TruncHour extends SqlToken {
    private SqlToken time;

    public TruncHour(SqlToken time) {
        this.time = time;
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        time.collectFields(fields);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
        switch(time.type()) {
        case Date:
        case Datetime:
            switch(vendor) {
            case Oracle:
                return "Trunc(" + time.format(vendor, options) + ", 'HH24')";
            case Postgres:
                return "date_trunc('hour', " + time.format(vendor, options) + ")";
            case SqlServer:
                return "Convert(datetime, convert(varchar(13)," + time.format(vendor, options) + ", 120) + ':00:00', 120)";
            default:
                throw new UnknownDatabaseException();
            }

        case Datespan:
            return new Mul(new Day(time), Operation.Mul, new SqlConst(new integer(datespan.TicksPerDay))).format(vendor,
                    options);

        default:
            throw new UnsupportedException();
        }
    }

    @Override
    public FieldType type() {
        return time.type();
    }
}
