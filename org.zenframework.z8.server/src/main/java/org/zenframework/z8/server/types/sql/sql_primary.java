package org.zenframework.z8.server.types.sql;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;

public class sql_primary extends SqlToken {
    private SqlToken token;

    public sql_primary() {}

    public sql_primary(SqlToken token) {
        this.token = token;
    }

    public SqlToken getToken() {
        return token;
    }

    public void setToken(SqlToken token) {
        this.token = token;
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        token.collectFields(fields);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
        return token.format(vendor, options, logicalContext);
    }

    @Override
    public String formula() {
        return getToken().formula();
    }

    @Override
    public FieldType type() {
        return token.type();
    }

    public sql_string z8_toString() {
        throw new UnsupportedOperationException();
    }

}
