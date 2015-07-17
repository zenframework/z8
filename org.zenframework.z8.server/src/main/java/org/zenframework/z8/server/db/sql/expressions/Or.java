package org.zenframework.z8.server.db.sql.expressions;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.If;
import org.zenframework.z8.server.types.sql.sql_integer;

public class Or extends Expression {
    static public String sqlOr = "or";

    public Or(SqlToken left, SqlToken right) {
        super(left, Operation.Or, right);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
        String result = left.format(vendor, options, true) + " OR " + right.format(vendor, options, true);

        if(!logicalContext) {
            SqlToken token = new If(new SqlStringToken(result), new sql_integer(1), new sql_integer(0));
            return token.format(vendor, options, false);
        }

        return result;
    }

    @Override
    public String formula() {
        return left.formula() + "||" + right.formula();
    }

    @Override
    public FieldType type() {
        return FieldType.Boolean;
    }
}
