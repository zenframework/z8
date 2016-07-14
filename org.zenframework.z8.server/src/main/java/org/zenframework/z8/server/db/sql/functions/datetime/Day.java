package org.zenframework.z8.server.db.sql.functions.datetime;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Mul;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.functions.conversion.ToNumber;
import org.zenframework.z8.server.db.sql.functions.numeric.Round;
import org.zenframework.z8.server.exceptions.UnsupportedException;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.integer;

public class Day extends SqlToken {
    private SqlToken date;

    public Day(SqlToken date) {
        this.date = date;
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        date.collectFields(fields);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
        switch(date.type()) {
        case Date:
        case Datetime:
            switch(vendor) {
            case Oracle:
                return new ToNumber(new SqlStringToken("TO_CHAR(" + date.format(vendor, options) + ", 'DD')")).format(
                        vendor, options);
            case SqlServer:
                return "Day(" + date.format(vendor, options) + ")";
            default:
                throw new UnknownDatabaseException();
            }

        case Datespan:
            return new Round(new Mul(date, Operation.Div, new SqlConst(new integer(datespan.TicksPerDay))), null).format(
                    vendor, options);

        default:
            throw new UnsupportedException();
        }
    }

    @Override
    public FieldType type() {
        return FieldType.Integer;
    }

    @Override
    public String formula() {
        return "(" + date.formula() + ").days()";
    }
}
