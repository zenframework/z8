package org.zenframework.z8.server.db.sql.functions;

import org.zenframework.z8.server.db.sql.SqlToken;

public class Min extends Window {
    public Min(SqlToken token) {
        super(token, "min");
    }
}
