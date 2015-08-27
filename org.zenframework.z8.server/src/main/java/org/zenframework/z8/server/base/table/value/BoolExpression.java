package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.sql.sql_bool;

public class BoolExpression extends Expression {
    public static class CLASS<T extends BoolExpression> extends Expression.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(BoolExpression.class);
            setAttribute(Native, BoolExpression.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new BoolExpression(container);
        }
    }

    public BoolExpression(IObject container) {
        super(container);
        width.set(5);
        stretch.set(false);
        setDefault(new bool());
    }

    @Override
    public FieldType type() {
        return FieldType.Boolean;
    }

    public sql_bool sql_bool() {
        return new sql_bool(new SqlField(this));
    }

    @Override
    public primary get() {
        return z8_get();
    }

    public bool z8_get() {
        return (bool)internalGet();
    }

    public BoolExpression.CLASS<? extends BoolExpression> operatorAssign(bool value) {
        set(value);
        return (BoolExpression.CLASS<?>) this.getCLASS();
    }

}
