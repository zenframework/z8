package org.zenframework.z8.server.types.sql;

import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Group;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.functions.conversion.GuidToChar;
import org.zenframework.z8.server.types.guid;

public class sql_guid extends sql_primary {
    public sql_guid() {
        super(new SqlConst(new guid()));
    }

    public sql_guid(String value) {
        super(new SqlConst(new guid(value)));
    }

    public sql_guid(guid value) {
        super(new SqlConst(value));
    }

    public sql_guid(SqlToken token) {
        super(token);
    }

    @Override
    public sql_string z8_toString() {
        return new sql_string(new GuidToChar(this));
    }

    public sql_guid operatorPriority() {
        return new sql_guid(new Group(this));
    }

    public sql_bool operatorEqu(sql_guid value) {
        return new sql_bool(new Rel(this, Operation.Eq, value));
    }

    public sql_bool operatorNotEqu(sql_guid value) {
        return new sql_bool(new Rel(this, Operation.NotEq, value));
    }
}
