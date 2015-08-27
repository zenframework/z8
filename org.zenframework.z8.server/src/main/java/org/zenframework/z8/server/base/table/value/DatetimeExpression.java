package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.format.Format;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.sql.sql_datetime;

public class DatetimeExpression extends Expression {
    public static class CLASS<T extends DatetimeExpression> extends Expression.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(DatetimeExpression.class);
            setAttribute(Native, DatetimeExpression.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new DatetimeExpression(container);
        }
    }

    public DatetimeExpression(IObject container) {
        super(container);
        format.set(Format.datetime);
        stretch.set(false);

        setDefault(new datetime(datetime.MIN));
    }

    @Override
    public FieldType type() {
        return FieldType.Datetime;
    }

    public sql_datetime sql_datetime() {
        return new sql_datetime(new SqlField(this));
    }

    @Override
    public primary get() {
        return z8_get();
    }

    public datetime z8_get() {
        return (datetime)internalGet();
    }

    public DatetimeExpression.CLASS<? extends DatetimeExpression> operatorAssign(datetime value) {
        set(value);
        return (DatetimeExpression.CLASS<?>) this.getCLASS();
    }
}
