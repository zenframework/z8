package org.zenframework.z8.server.db.sql.functions.numeric;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.Clause;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.WrappedClause;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.sql.sql_integer;

public class Truncate extends SqlToken {
    private SqlToken param1;
    private SqlToken param2;

    public Truncate(SqlToken p1, SqlToken p2) {
        param1 = p1;
        param2 = p2;
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        param1.collectFields(fields);
        param2.collectFields(fields);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
        Clause clause;
        switch(vendor) {
        case Oracle:
            clause = new WrappedClause(", ", "trunc(", ")");
            break;
        case SqlServer:
            clause = new WrappedClause(", ", "round(", ", 1)");
            break;
        default:
            throw new UnknownDatabaseException();
        }
       
        clause.add(param1);
        if(param2 != null)
            clause.add(param2);
        else
            clause.add(new sql_integer(0));
        
        return clause.format(vendor, options);
    }

    @Override
    public FieldType type() {
        return param1.type();
    }
}
