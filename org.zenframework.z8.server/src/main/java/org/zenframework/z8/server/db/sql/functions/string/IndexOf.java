package org.zenframework.z8.server.db.sql.functions.string;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Add;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.functions.Nvl;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.integer;

public class IndexOf extends SqlToken {
    private SqlToken what;
    private SqlToken where;
    private SqlToken from;

    public IndexOf(SqlToken what, SqlToken where, SqlToken from) {
        this.what = what;
        this.where = where;
        this.from = from;
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        this.what.collectFields(fields);
        this.where.collectFields(fields);
        this.from.collectFields(fields);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
        String result;
        
        if(vendor == DatabaseVendor.Oracle) {
            from = from != null ? new Add(from, Operation.Add, new SqlConst(new integer(1))) : 
                new SqlConst(new integer(1));

            result = "instr(" + what.format(vendor, options, logicalContext) + ", " + 
                where.format(vendor, options, logicalContext) + ", " + 
                from.format(vendor, options, logicalContext) + ") - 1";
        } else if(vendor == DatabaseVendor.SqlServer) {
            from = from != null ? from : new SqlConst(new integer(0));

            result = "charIndex(" + what.format(vendor, options, logicalContext) + ", " + 
                where.format(vendor, options, logicalContext) + ", " + 
                from.format(vendor, options, logicalContext) + ") - 1";
        } else if(vendor == DatabaseVendor.Postgres) {
            where = from != null ? new Substr(where, from) : where;
            result = "position(" + what.format(vendor, options, logicalContext) + " in " + 
                where.format(vendor, options, logicalContext) + ") - 1";
        } else
            throw new UnknownDatabaseException();

        return (new Nvl(new SqlStringToken(result), new SqlConst(new integer(-1)))).format(vendor, options);
    }

    @Override
    public FieldType type() {
        return FieldType.Integer;
    }
}
