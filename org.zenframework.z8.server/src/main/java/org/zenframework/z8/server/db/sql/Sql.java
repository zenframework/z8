package org.zenframework.z8.server.db.sql;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Group;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Or;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.expressions.Unary;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_guid;
import org.zenframework.z8.server.types.sql.sql_string;

public class Sql {
    public static SqlToken equals(Field field, string string) {
        return new Group(new Rel(field, Operation.Eq, new sql_string(string)));
    }

    public static SqlToken equals(Field field, guid guid) {
        return new Group(new Rel(field, Operation.Eq, new sql_guid(guid)));
    }

    public static SqlToken and(SqlToken left, SqlToken right) {
        return new Group(new And(left, right));
    }

    public static SqlToken or(SqlToken left, SqlToken right) {
        return new Group(new Or(left, right));
    }

    public static SqlToken not(Field field) {
        return new Unary(Operation.Not, new SqlField(field));
    }
}
