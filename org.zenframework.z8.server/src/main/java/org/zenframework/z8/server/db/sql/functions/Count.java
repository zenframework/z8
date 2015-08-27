package org.zenframework.z8.server.db.sql.functions;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Count extends SqlToken {
    private SqlToken token;

    public Count(Field field) {
        this(new SqlField(field));
    }

    public Count(SqlToken token) {
        this.token = token;
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        token.collectFields(fields);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext)
            throws UnknownDatabaseException {
        return "count(" + token.format(vendor, options) + ")";
    }

    @Override
    public FieldType type() {
        return FieldType.Integer;
    }
}
