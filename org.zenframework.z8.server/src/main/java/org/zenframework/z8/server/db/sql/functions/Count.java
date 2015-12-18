package org.zenframework.z8.server.db.sql.functions;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;

public class Count extends Window {
    public Count(Field field) {
        this(new SqlField(field));
    }

    public Count(SqlToken token) {
        super(token, "count");
    }
}
