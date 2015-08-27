package org.zenframework.z8.server.db.sql.expressions;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.If;
import org.zenframework.z8.server.exceptions.UnsupportedException;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.sql.sql_integer;

public class Unary extends SqlToken {
    private Operation sqlOper;
    private SqlToken left;

    public Unary(Operation oper, SqlToken l) {
        sqlOper = oper;
        left = l;
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        if(left != null) {
            left.collectFields(fields);
        }
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext)
            throws UnknownDatabaseException {
        switch(sqlOper) {
        case Not: {
            SqlToken t = new UNOTToken();
            if(!logicalContext) {
                t = new If(t, new sql_integer(1), new sql_integer(0));
            }
            return t.format(vendor, options, logicalContext);
        }

        case Minus: {
            return "(-" + left.format(vendor, options) + ")";
        }

        default:
            throw new UnsupportedException();
        }
    }

    private String sign() {
        switch(sqlOper) {
        case Not:
            return "!";
        case Minus:
            return "-";
        default:
            throw new UnsupportedException();
        }
    }

    @Override
    public String formula() {
        return sign() + left.formula();
    }

    @Override
    public FieldType type() {
        switch(sqlOper) {
        case Not: {
            return FieldType.Boolean;
        }

        case Minus: {
            return left.type();
        }

        default:
            throw new UnsupportedException();
        }
    }

    private class UNOTToken extends SqlToken {
        @Override
        public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
            return "not(" + left.format(vendor, options, true) + ")";
        }

        @Override
        public String formula() {
            assert (false);
            return null;
        }

        @Override
        public void collectFields(Collection<IValue> fields) {}

        @Override
        public FieldType type() {
            return FieldType.Boolean;
        }
    }
}
