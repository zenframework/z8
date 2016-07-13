package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_string;

public class StringExpression extends Expression {
    public static class CLASS<T extends StringExpression> extends Expression.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(StringExpression.class);
        }

        @Override
        public Object newObject(IObject container) {
            return new StringExpression(container);
        }
    }

    public StringExpression(IObject container) {
        super(container);

        setDefault(new string());
        aggregation = Aggregation.Max;
    }

    @Override
    public FieldType type() {
        return FieldType.String;
    }

    public sql_string sql_string() {
        return new sql_string(new SqlField(this));
    }

    @Override
    public primary get() {
        return z8_get();
    }

    public string z8_get() {
        return (string)internalGet();
    }

    public StringExpression.CLASS<? extends StringExpression> operatorAssign(string value) {
        set(value);
        return (StringExpression.CLASS<?>) this.getCLASS();
    }
}
