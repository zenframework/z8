package org.zenframework.z8.server.types.sql;

import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.conversion.ToChar;
import org.zenframework.z8.server.types.binary;

public class sql_binary extends sql_primary {
    public sql_binary() {
        super(new SqlConst(new binary()));
    }

    public sql_binary(binary value) {
        super(new SqlConst(value));
    }

    public sql_binary(SqlToken token) {
        super(token);
    }

    @Override
    public sql_string z8_toString() {
        return new sql_string(new ToChar(this));
    }
}
