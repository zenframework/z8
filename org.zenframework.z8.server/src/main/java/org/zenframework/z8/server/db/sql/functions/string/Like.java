package org.zenframework.z8.server.db.sql.functions.string;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.conversion.ToChar;

public class Like extends SqlToken {
    private SqlToken value;
    private SqlToken like;
    private SqlToken escape;

    public Like(SqlToken value, SqlToken like, SqlToken escape) {
        this.value = value;
        this.like = like;
        this.escape = escape;
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        value.collectFields(fields);
        like.collectFields(fields);
        if(escape != null)
            escape.collectFields(fields);
    }

    @Override
    public FieldType type() {
        return FieldType.Boolean;
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
        SqlToken valueToken = value.type() != FieldType.String ? new ToChar(value) : value;
        SqlToken likeToken = like.type() != FieldType.String ? new ToChar(like) : like;
        SqlToken escapeToken = escape != null && escape.type() != FieldType.String ? new ToChar(escape) : escape;
 
        return valueToken.format(vendor, options) + " LIKE " + likeToken.format(vendor, options)
                + (escapeToken == null ? "" : " ESCAPE " + escapeToken.format(vendor, options));
    }
}
