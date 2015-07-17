package org.zenframework.z8.server.db.sql.expressions;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.conversion.ToNumber;
import org.zenframework.z8.server.db.sql.functions.numeric.Round;
import org.zenframework.z8.server.types.integer;

abstract public class Expression extends SqlToken {
    protected Operation operation;
    protected SqlToken left;
    protected SqlToken right;

    public Expression(SqlToken left, Operation operation, SqlToken right) {
        this.left = left;
        SetNewExpression(operation, right);
    }

    public Operation getSQLOperation() {
        return operation;
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        if(left != null) {
            left.collectFields(fields);
        }

        if(right != null) {
            right.collectFields(fields);
        }
    }

    public SqlToken getSQLRight() {
        return right;
    }

    public void SetNewExpression(Operation operation, SqlToken right) {
        this.operation = operation;

        if(IsDateSpanWithDate(left, right)) {
            left = ConvertDateSpanToDate(left);
        }
        if(IsDateSpanWithDate(right, left)) {
            this.right = ConvertDateSpanToDate(right);
        }
        else {
            this.right = right;
        }
    }

    private boolean IsDateSpanWithDate(SqlToken l, SqlToken r) {
        return (l != null) && (r != null) && (l.type() == FieldType.Datespan)
                && ((r.type() == FieldType.Date) || (r.type() == FieldType.Datetime));
    }

    private SqlToken ConvertDateSpanToDate(SqlToken t) {
        integer hourInDay = new integer(24 * 60 * 60 * 1000);
        return new DateSpanRound(new Mul(new ToNumber(t), Operation.Div, new SqlConst(hourInDay)), new SqlConst(new integer(
                5)));
    }

    private class DateSpanRound extends Round {
        DateSpanRound(SqlToken p1, SqlToken p2) {
            super(p1, p2);
        }

        @Override
        public FieldType type() {
            return FieldType.Datespan;
        }
    }
}
