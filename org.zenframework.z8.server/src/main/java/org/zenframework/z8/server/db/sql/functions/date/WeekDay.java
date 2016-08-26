package org.zenframework.z8.server.db.sql.functions.date;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.If;
import org.zenframework.z8.server.db.sql.functions.conversion.ToNumber;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class WeekDay extends SqlToken {
    private SqlToken param1;

    public WeekDay(SqlToken p1) {
        param1 = p1;
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
        switch(vendor) {
        case Oracle:
            return new ToNumber(new SqlStringToken("TO_CHAR(" + param1.format(vendor, options) + ",'D')")).format(vendor,
                    options);
        case SqlServer:
            return new If(new FuncToken("(@@DATEFIRST + DATEPART(dw, " + param1.format(vendor, options) + ") - 1) < 8"),
                    new FuncToken("(@@DATEFIRST + DATEPART(dw, " + param1.format(vendor, options) + ") - 1)"),
                    new FuncToken("(@@DATEFIRST + DATEPART(dw, " + param1.format(vendor, options) + ") - 8)")).format(
                    vendor, options);
        default:
            throw new UnknownDatabaseException();
        }
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        param1.collectFields(fields);
    }

    @Override
    public FieldType type() {
        return FieldType.Integer;
    }

    private class FuncToken extends SqlToken {
        private String Func;

        private FuncToken(String _func) {
            Func = _func;
        }

        @Override
        public void collectFields(Collection<IValue> fields) {}

        @Override
        public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext)
                throws UnknownDatabaseException {
            return Func;
        }

        @Override
        public FieldType type() {
            return FieldType.Integer;
        }
    }
}
