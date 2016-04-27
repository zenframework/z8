package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_string;

public class TextExpression extends Expression {
    public static class CLASS<T extends TextExpression> extends Expression.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(TextExpression.class);
            setAttribute(Native, TextExpression.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new TextExpression(container);
        }
    }

    public integer lines = new integer(5);

    public TextExpression(IObject container) {
        super(container);

        setDefault(new string());

        length.set(0);
        aggregation = Aggregation.None;
    }

    @Override
    public FieldType type() {
        return FieldType.Text;
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

    public TextExpression.CLASS<? extends TextExpression> operatorAssign(string value) {
        set(value);
        return (TextExpression.CLASS<?>) this.getCLASS();
    }
}
