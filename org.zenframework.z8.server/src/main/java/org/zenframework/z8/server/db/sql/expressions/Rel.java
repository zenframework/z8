package org.zenframework.z8.server.db.sql.expressions;

import java.util.HashMap;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.If;
import org.zenframework.z8.server.types.sql.sql_integer;

public class Rel extends Expression {
    final static HashMap<Operation, String> operations = new HashMap<Operation, String>();
    final static HashMap<Operation, String> formulaOperations = new HashMap<Operation, String>();

    static {
        operations.put(Operation.Eq, "=");
        operations.put(Operation.NotEq, "<>");
        operations.put(Operation.LT, "<");
        operations.put(Operation.GT, ">");
        operations.put(Operation.LE, "<=");
        operations.put(Operation.GE, ">=");

        formulaOperations.put(Operation.Eq, "==");
        formulaOperations.put(Operation.NotEq, "<>");
        formulaOperations.put(Operation.LT, "<");
        formulaOperations.put(Operation.GT, ">");
        formulaOperations.put(Operation.LE, "<=");
        formulaOperations.put(Operation.GE, ">=");
    }

    public Rel(Field l, Operation oper, Field r) {
        this(new SqlField(l), oper, new SqlField(r));
    }

    public Rel(Field l, Operation oper, SqlToken r) {
        this(new SqlField(l), oper, r);
    }

    public Rel(SqlToken l, Operation oper, Field r) {
        this(l, oper, new SqlField(r));
    }

    public Rel(SqlToken l, Operation oper, SqlToken r) {
        super(l, oper, r);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {

        String rel = left.format(vendor, options) + operations.get(this.operation) + right.format(vendor, options);
        
        if(!logicalContext)
            return new If(new SqlStringToken(rel), new sql_integer(1), new sql_integer(0)).format(vendor, options);

        return rel;
    }

    @Override
    public String formula() {
        return left.formula() + formulaOperations.get(operation) + right.formula();
    }

    @Override
    public FieldType type() {
        return FieldType.Boolean;
    }
}
