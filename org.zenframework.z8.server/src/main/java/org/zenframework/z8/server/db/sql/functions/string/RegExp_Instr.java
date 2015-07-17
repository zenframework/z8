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
import org.zenframework.z8.server.db.sql.functions.Nvl;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.integer;

public class RegExp_Instr extends SqlToken {
    private SqlToken str;
    private SqlToken pattern;

    public RegExp_Instr(SqlToken _str, SqlToken _pattern) {
        str = _str;
        pattern = _pattern;
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        str.collectFields(fields);
        pattern.collectFields(fields);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
        Clause clause;
        switch(vendor) {
        case Oracle: {
            clause = new WrappedClause(", ", "regexp_instr(", ")-1");
            clause.add(pattern);
            clause.add(str);
        }
            break;

        case SqlServer: {
            clause = new WrappedClause(", ", "patIndex(", ")-1");
            clause.add(pattern);
            clause.add(str);
        }
            break;

        default:
            throw new UnknownDatabaseException();
        }
        return (new Nvl(clause, new SqlConst(new integer(-1)))).format(vendor, options);
    }

    @Override
    public FieldType type() {
        return FieldType.Integer;
    }
}
