package org.zenframework.z8.server.db.sql.expressions;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.string.Concat;
import org.zenframework.z8.server.exceptions.UnsupportedException;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.datespan;

public class Add extends Operator {
    public Add(SqlToken left, Operation operation, SqlToken right) {
        super(left, operation, right);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
        switch(operation) {
        case Add: {
            if(left.type() == FieldType.String) {
                return new Concat(left, right).format(vendor, options);
            }
            return left.format(vendor, options) + "+" + right.format(vendor, options);
        }

        case Sub: {
            String left = this.left.format(vendor, options);
            String right = this.right.format(vendor, options);

            if(this.left.isDate() && this.right.isDate()) {
                switch(vendor) {
                case Oracle:
                    return "((EXTRACT(DAY FROM (" + left + "-" + right + ") DAY TO SECOND)*" + datespan.TicksPerDay
                            + ")+(EXTRACT(HOUR FROM (" + left + "-" + right + ") DAY TO SECOND)*" + datespan.TicksPerHour
                            + ")+(EXTRACT(MINUTE FROM (" + left + "-" + right + ") DAY TO SECOND)*"
                            + datespan.TicksPerMinute + ")+(EXTRACT(SECOND FROM (" + left + "-" + right
                            + ") DAY TO SECOND)*" + datespan.TicksPerSecond + "))";
                case SqlServer:
                    return "((convert(numeric(19,0), DATEDIFF(mi, " + right + ", " + left + "))*" + datespan.TicksPerMinute
                            + ")+DatePart(ss, " + left + "-" + right + ")*" + datespan.TicksPerSecond + "+DatePart(ms, "
                            + left + "-" + right + "))";
                default:
                    throw new UnknownDatabaseException();
                }
            }

            return left + "-" + right;
        }

        default:
            throw new UnsupportedException();
        }
    }

    private String sign() {
        switch(operation) {
        case Add:
            return "+";
        case Sub:
            return "-";
        default:
            throw new UnsupportedException();
        }
    }

    @Override
    public String formula() {
        return left.formula() + sign() + right.formula();
    }
}
