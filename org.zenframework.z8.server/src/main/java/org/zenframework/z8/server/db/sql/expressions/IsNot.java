package org.zenframework.z8.server.db.sql.expressions;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.types.sql.sql_bool;

public class IsNot extends Rel {
    public IsNot(Field field) {
        this(new SqlField(field));
    }

    public IsNot(SqlToken token) {
        super(token, Operation.NotEq, new sql_bool(true));
    }
}
