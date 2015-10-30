package org.zenframework.z8.server.db.sql.functions.conversion;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.CaseToken;
import org.zenframework.z8.server.db.sql.functions.string.RegIndexOf;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.sql.sql_string;

public class IsIntegerStr extends SqlToken {
    private SqlToken string;

    public IsIntegerStr(SqlToken string) {
        this.string = string;
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        string.collectFields(fields);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
        CaseToken CaseToken = new CaseToken();
        CaseToken.addWhen(
                new SqlStringToken(new RegIndexOf(string, new sql_string(pattern())).format(vendor, options,
                        logicalContext) + " < 0"), new SqlConst(new integer(1)));
        CaseToken.setElse(new SqlConst(new integer(0)));
        String result = CaseToken.format(vendor, options);
        if(logicalContext) {
            result = "(" + result + ") = 1"; // Это случай использования в Where
        }
        return result;
    }

    @Override
    public FieldType type() {
        return FieldType.Boolean;
    }

    protected String pattern() {
        return "%[^0-9]%";
    }
}
