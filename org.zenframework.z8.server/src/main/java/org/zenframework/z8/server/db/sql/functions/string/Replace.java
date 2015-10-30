package org.zenframework.z8.server.db.sql.functions.string;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Replace extends StringFunction {
    private SqlToken string;
    private SqlToken pattern;
    private SqlToken replacement;

    public Replace(SqlToken string, SqlToken pattern, SqlToken replacement) {
        this.string = string;
        this.pattern = pattern;
        this.replacement = replacement;
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        string.collectFields(fields);
        pattern.collectFields(fields);
        replacement.collectFields(fields);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
        switch(vendor) {
        case Oracle:
        case SqlServer:
        case Postgres:
            return "replace(" + 
                string.format(vendor, options, logicalContext) + ", " +
                pattern.format(vendor, options, logicalContext) + ", " +
                replacement.format(vendor, options, logicalContext) + ")";
        default:
            throw new UnknownDatabaseException();
        }
    }

    @Override
    public String formula() {
        return string.formula() + ".replace(" + pattern.formula() + ", " + replacement.formula() + ")";
    }

}
