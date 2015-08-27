package org.zenframework.z8.server.db.sql.functions.conversion;

import org.zenframework.z8.server.db.sql.SqlToken;

public class IsNumericStr extends IsIntegerStr {
    public IsNumericStr(SqlToken _param1) {
        super(_param1);
    }

    @Override
    protected String pattern() {
        return "%[^0-9|.]%";
    }
}
