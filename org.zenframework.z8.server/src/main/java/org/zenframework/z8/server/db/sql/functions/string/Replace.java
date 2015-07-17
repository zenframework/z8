package org.zenframework.z8.server.db.sql.functions.string;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.sql.Clause;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.WrappedClause;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.string;

public class Replace extends StringFunction {
    private SqlToken param1;
    private SqlToken param2;
    private SqlToken param3;

    public Replace(SqlToken p1, SqlToken p2, SqlToken p3) {
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
        Clause clause;
        switch(vendor) {
        case Oracle:
        case SqlServer:
            clause = new WrappedClause(", ", "replace(", ")");
            break;
        default:
            throw new UnknownDatabaseException();
        }
        clause.add(param1);
        clause.add(param2);
        if(param3 != null)
            clause.add(param3);
        else
            clause.add(new SqlConst(new string("")));
        return clause.format(vendor, options);
    }

    @Override
    public String formula() {
        return param1.formula() + ".replace(" + param2.formula() + ", " + param3.formula() + ")";
    }

}
