package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.format.Format;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_date;

public class DatetimeExpression extends Expression {
    public static class CLASS<T extends DatetimeExpression> extends Expression.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(DatetimeExpression.class);
        }

        @Override
        public Object newObject(IObject container) {
            return new DatetimeExpression(container);
        }
    }

    public DatetimeExpression(IObject container) {
        super(container);
        format = new string(Format.datetime);
        stretch = new bool(false);

        setDefault(new date(date.MIN));
    }

    @Override
    public FieldType type() {
        return FieldType.Datetime;
    }

    public sql_date sql_date() {
        return new sql_date(new SqlField(this));
    }

    @Override
    public primary get() {
        return z8_get();
    }

    public date z8_get() {
        return (date)internalGet();
    }

    public DatetimeExpression.CLASS<? extends DatetimeExpression> operatorAssign(date value) {
        set(value);
        return (DatetimeExpression.CLASS<?>) this.getCLASS();
    }
}
