package org.zenframework.z8.server.db.sql.expressions;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlToken;

abstract public class Operator extends Expression {
    private FieldType type = FieldType.None;

    public Operator(SqlToken l, Operation oper, SqlToken r) {
        super(l, oper, r);
    }

    @Override
    public FieldType type() {
        if(type == FieldType.None) {
            if(left.isDecimal() || right.isDecimal()) {
                type = FieldType.Decimal;
            }
            else if(operation == Operation.Sub && left.isDate() && right.isDate()) {
                type = FieldType.Datespan;
            }
            else if(left.isDate() && right.isDatespan()) {
                type = left.type();
            }
            else if(right.isDate() && left.isDatespan()) {
                type = right.type();
            }
            else if(left.isNumeric() && right.isDatespan() || right.isNumeric() && left.isDatespan()) {
                type = FieldType.Datespan;
            }
            else if(left.type() == right.type()) {
                type = left.type();
            }
            
            assert(false);
        }

        return type;
    }
}
