package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_primary;
import org.zenframework.z8.server.types.sql.sql_string;

public class StringField extends Field {
    static public int DefaultLength = 30;

    public static class CLASS<T extends StringField> extends Field.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(StringField.class);
            setAttribute(Native, StringField.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new StringField(container);
        }
    }

    public integer minLength = new integer(0);
    public integer maxLength = new integer(0);

    public StringField(IObject container) {
        super(container);
        setDefault(new string(""));
        length.set(DefaultLength);
        aggregation = Aggregation.Max;
    }

    public string z8_getDefault() {
        return (string)super.getDefault();
    }

    @Override
    public primary getDefault() {
        return z8_getDefault();
    }

    @Override
    public FieldType type() {
        return FieldType.String;
    }

    @Override
    public int size() {
        return length.getInt();
    }

    @Override
    public String sqlType(DatabaseVendor vendor) {
        String name = type().vendorType(vendor);
        return name + "(" + length.get() + ")";
    }

    public sql_string sql_string() {
        return new sql_string(new SqlField(this));
    }

    public void set(String value) {
        set(new string(value));
    }

    @Override
    public primary get() {
        return z8_get();
    }

    public string z8_get() {
        return (string)internalGet();
    }

    @Override
    public sql_primary formula() {
        return z8_formula();
    }

    public sql_string z8_formula() {
        return null;
    }

    public StringField.CLASS<? extends StringField> operatorAssign(string value) {
        set(value);
        return (StringField.CLASS<?>) this.getCLASS();
    }

    @Override
    public void writeMeta(JsonObject writer) {
        super.writeMeta(writer);

        writer.put(Json.min, Math.max(minLength.get(), 0));
        writer.put(Json.max, Math.min(maxLength.get(), length.get()));
    }
}
