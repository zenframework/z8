package org.zenframework.z8.server.db.sql.expressions;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.numeric.Mod;
import org.zenframework.z8.server.exceptions.UnsupportedException;

public class Mul extends Operator {
    public Mul(SqlToken l, Operation oper, SqlToken r) {
        super(l, oper, r);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
        switch(operation) {
        case Mul: {
            return left.format(vendor, options) + "*" + right.format(vendor, options);
        }

        case Div: {
            return left.format(vendor, options) + "/" + right.format(vendor, options);
        }

        case Mod: {
            return new Mod(left, right).format(vendor, options);
        }

        default:
            throw new UnsupportedException();
        }
    }

    private String sign() {
        switch(operation) {
        case Mul:
            return "*";
        case Div:
            return "/";
        case Mod:
            return "%";
        default:
            throw new UnsupportedException();
        }
    }

    @Override
    public String formula() {
        return left.formula() + sign() + right.formula();
    }

}
