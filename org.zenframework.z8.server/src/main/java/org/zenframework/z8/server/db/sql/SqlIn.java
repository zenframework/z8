package org.zenframework.z8.server.db.sql;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.functions.In;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class SqlIn extends SqlToken {
    private In inToken = new In();

    public SqlIn(SqlToken param1, SqlToken _param2) {
        inToken.setCondition(param1);
        inToken.addValues(_param2);
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        inToken.collectFields(fields);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext)
            throws UnknownDatabaseException {
        return inToken.format(vendor, options, logicalContext);
    }

    @Override
    public FieldType type() {
        return FieldType.Boolean;
    }
}
