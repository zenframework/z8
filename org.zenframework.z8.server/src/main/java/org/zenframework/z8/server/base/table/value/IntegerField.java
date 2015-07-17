package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.format.Format;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.sql.sql_integer;
import org.zenframework.z8.server.types.sql.sql_primary;

public class IntegerField extends Field {
    public static class CLASS<T extends IntegerField> extends Field.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(IntegerField.class);
            setAttribute(Native, IntegerField.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new IntegerField(container);
        }
    }

    public IntegerField(IObject container) {
        super(container);
        setDefault(new integer());
        format.set(Format.integer);
        stretch.set(false);
    }

    public integer z8_getDefault() {
        return (integer)super.getDefault();
    }

    @Override
    public primary getDefault() {
        return z8_getDefault();
    }

    @Override
    public FieldType type() {
        return FieldType.Integer;
    }

    @Override
    public String sqlType(DatabaseVendor vendor) {
        String name = type().vendorType(vendor);

        if(vendor == DatabaseVendor.Oracle) {
            return name + "(19, 0)";
        }
        return name;
    }

    public sql_integer sql_int() {
        return new sql_integer(new SqlField(this));
    }

    @Override
    public primary get() {
        return z8_get();
    }

    public integer z8_get() {
        return (integer)internalGet();
    }

    @Override
    public sql_primary formula() {
        return z8_formula();
    }

    public sql_integer z8_formula() {
        return null;
    }

    public IntegerField.CLASS<? extends IntegerField> operatorAssign(integer value) {
        set(value);
        return (IntegerField.CLASS<?>) this.getCLASS();
    }
}
