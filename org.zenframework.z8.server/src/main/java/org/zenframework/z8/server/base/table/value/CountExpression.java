package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.sql.sql_integer;

public class CountExpression extends IntegerExpression {
    public static class CLASS<T extends CountExpression> extends IntegerExpression.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(CountExpression.class);
        }

        @Override
        public Object newObject(IObject container) {
            return new CountExpression(container);
        }
    }

    public Field.CLASS<? extends Field> field = null;
    
    public CountExpression() {
        this(null);
    }

    public CountExpression(IObject container) {
        super(container);

        aggregation = Aggregation.Count;
    }

    @Override
    protected SqlToken z8_expression() {
        return field != null ? new SqlField(field.get()) : new sql_integer(1);
    }
}
