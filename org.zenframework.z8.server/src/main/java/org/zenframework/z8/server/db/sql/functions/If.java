package org.zenframework.z8.server.db.sql.functions;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;

public class If extends SqlToken {
    private SqlToken param1, param2, param3;

    public If(SqlToken p1, SqlToken p2, SqlToken p3) {
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
        CaseToken CaseToken = new CaseToken();
        CaseToken.addWhen(param1, param2);
        CaseToken.setElse(param3);

        String result = CaseToken.format(vendor, options);

        if(logicalContext) {
            result = "(" + result + ")=1"; //  Where
        }

        return result;
    }

    @Override
    public String formula() {
        return "(" + param1.formula() + " ? (" + param2.formula() + ") : (" + param3.formula() + "))";
    }

    @Override
    public FieldType type() {
        return param2.type();
    }
}
