package org.zenframework.z8.server.db.sql.functions.string;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.string;

public class LPad extends StringFunction {
    private SqlToken param1;
    private SqlToken param2;
    private SqlToken param3;

    public LPad(SqlToken p1, SqlToken p2, SqlToken p3) {
        param1 = p1;
        param2 = p2;
        param3 = p3;
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        param1.collectFields(fields);
        param2.collectFields(fields);
        param3.collectFields(fields);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
        param3 = (param3 == null ? new SqlConst(new string(" ")) : param3);
        switch(vendor) {
        case Oracle:
            return "LPAD(" + param1.format(vendor, options) + "," + param2.format(vendor, options) + ","
                    + param3.format(vendor, options) + ")";
        case SqlServer:
            return "RIGHT(REPLICATE(" + param3.format(vendor, options) + "," + param2.format(vendor, options) + ")+"
                    + param1.format(vendor, options) + "," + param2.format(vendor, options) + ")";
        default:
            throw new UnknownDatabaseException();
        }
    }
}
