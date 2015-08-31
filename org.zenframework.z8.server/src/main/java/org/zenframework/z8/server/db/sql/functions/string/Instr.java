package org.zenframework.z8.server.db.sql.functions.string;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.Clause;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.WrappedClause;
import org.zenframework.z8.server.db.sql.expressions.Add;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.functions.Nvl;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.integer;

public class Instr extends SqlToken {
    private SqlToken param1;
    private SqlToken param2;
    private SqlToken param3;

    public Instr(SqlToken p1, SqlToken p2, SqlToken p3) {
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
        case Oracle: {
            clause = new WrappedClause(", ", "instr(", ") - 1");
            clause.add(param1);
            clause.add(param2);
        }
            break;

        case SqlServer: {
            clause = new WrappedClause(", ", "charIndex(", ") - 1");
            clause.add(param2);
            clause.add(param1);
        }
            break;

        default:
            throw new UnknownDatabaseException();
        }
        SqlToken p3;
        if(param3 != null) {
            switch(vendor) {
            case Oracle:
                p3 = new Add(param3, Operation.Add, new SqlConst(new integer(1)));
                break;
            case SqlServer:
                p3 = param3;
                break;
            default:
                throw new UnknownDatabaseException();
            }
        }
        else {
            switch(vendor) {
            case Oracle:
                p3 = new SqlConst(new integer(1));
                break;
            case SqlServer:
                p3 = new SqlConst(new integer(0));
                break;
            default:
                throw new UnknownDatabaseException();
            }
        }
        clause.add(p3);
        return (new Nvl(clause, new SqlConst(new integer(-1)))).format(vendor, options);
    }

    @Override
    public FieldType type() {
        return FieldType.Integer;
    }
}
