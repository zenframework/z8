package org.zenframework.z8.server.db.sql.functions;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.sql.sql_bool;

public class InVector extends SqlToken {
    private In inToken = null;

    public InVector(Field field, RCollection<? extends primary> param2) {
        this(new SqlField(field), (Collection<? extends primary>) param2);
    }

    public InVector(SqlToken what, RCollection<? extends primary> values) {
        this(what, (Collection<? extends primary>) values);
    }

    public InVector(Field field, Collection<? extends primary> values) {
        this(new SqlField(field), values);
    }
    
    public InVector(SqlToken what, Collection<? extends primary> values) {
        if (values.size() != 0) {
            inToken = new In();
            inToken.setCondition(what);
            for (primary i : values)
                inToken.addValues(new SqlConst(i));
        }
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        if (inToken != null) {
            inToken.collectFields(fields);
        }
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
        if (inToken != null) {
            return inToken.format(vendor, options, logicalContext);
        } else {
            return new sql_bool(false).format(vendor, options, logicalContext);
        }
    }

    @Override
    public FieldType type() {
        return FieldType.Boolean;
    }
}
