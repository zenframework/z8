package org.zenframework.z8.server.db.sql.functions;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.integer;

public class In extends SqlToken {
    private SqlToken condition;
    private List<SqlToken> values = new LinkedList<SqlToken>();

    public In() {}

    public In(SqlToken condition, Collection<SqlToken> values) {
        this.condition = condition;
        this.values.addAll(values);
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        if(condition != null) {
            condition.collectFields(fields);
        }

        for(SqlToken value : values) {
            value.collectFields(fields);
        }
    }

    public void setCondition(SqlToken condition) {
        this.condition = condition;
    }

    public void addValues(SqlToken values) {
        this.values.add(values);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext)
            throws UnknownDatabaseException {
        SqlToken t = new InToken();

        if(!logicalContext) {
            t = new If(t, new SqlConst(new integer(1)), new SqlConst(new integer(0)));
        }

        return t.format(vendor, options, logicalContext);
    }

    @Override
    public FieldType type() {
        return FieldType.Boolean;
    }

    private class InToken extends SqlToken {
        @Override
        public void collectFields(Collection<IValue> fields) {}

        @Override
        public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext)
                throws UnknownDatabaseException {
            String frm = "(" + condition.format(vendor, options) + " in (";
            Iterator<SqlToken> t = values.iterator();
            while(t.hasNext())
                frm += t.next().format(vendor, options) + (t.hasNext() ? ", " : "");
            frm += "))";
            return frm;
        }

        @Override
        public FieldType type() {
            return FieldType.Boolean;
        }
    }
}
