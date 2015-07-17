package org.zenframework.z8.server.db.sql.functions.string;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Group;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Or;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class IsEmpty extends SqlToken {
    private SqlToken value;

    public IsEmpty(Field field) {
        this(new SqlField(field));
    }

    public IsEmpty(SqlToken value) {
        this.value = value;
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        value.collectFields(fields);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
        SqlToken t = new Group(new Or(new Rel(new Length(value), Operation.Eq, new SqlConst(new integer(0))), new Rel(
                new Group(value), Operation.Eq, new SqlConst(new string("")))));
        return t.format(vendor, options, logicalContext);
    }

    @Override
    public FieldType type() {
        return FieldType.Boolean;
    }

    @Override
    public String formula() {
        return "Z8.isEmpty(" + value.formula() + ")";
    }
}
