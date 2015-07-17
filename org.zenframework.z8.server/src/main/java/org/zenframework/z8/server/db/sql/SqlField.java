package org.zenframework.z8.server.db.sql;

import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.base.table.value.Expression;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.functions.Array;
import org.zenframework.z8.server.db.sql.functions.Average;
import org.zenframework.z8.server.db.sql.functions.Concat;
import org.zenframework.z8.server.db.sql.functions.Count;
import org.zenframework.z8.server.db.sql.functions.Max;
import org.zenframework.z8.server.db.sql.functions.Min;
import org.zenframework.z8.server.db.sql.functions.Sum;
import org.zenframework.z8.server.db.sql.functions.conversion.ToChar;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class SqlField extends SqlToken {
    private Field field = null;

    public SqlField(Field field) {
        this.field = field;
    }

    @Override
    public String formula() {
        return "{" + field.id() + "}";
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        if(field instanceof Expression) {
            Expression expression = (Expression)field;
            SqlToken token = expression.expression();

            if(token != null) {
                token.collectFields(fields);
            }
        } else
            fields.add(field);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext)
            throws UnknownDatabaseException {
        Aggregation aggregation = field.isAggregated() ? field.getAggregation() : Aggregation.None;

        Collection<IValue> fields = getUsedFields();
        Collection<IValue> aggregatedFields = new ArrayList<IValue>();

        for(IValue value : fields) {
            Field field = (Field)value;

            if(field.isAggregated()) {
                aggregatedFields.add(field);
                field.setAggregated(false);
            }
        }

        SqlToken token = getToken(vendor, options, logicalContext, aggregation);
        String result = aggregate(token, aggregation).format(vendor, options, logicalContext);

        aggregate(aggregatedFields);

        return result;
    }

    private SqlToken getToken(DatabaseVendor vendor, FormatOptions options, boolean logicalContext, Aggregation aggregation) {
        boolean isAliased = options.getFieldAlias(field) != null;
        String alias = field.format(vendor, options);

        if(field.type() == FieldType.Boolean) {
            return new SqlStringToken(alias + (logicalContext ? "=1" : ""));
        }

        SqlToken result = new SqlStringToken(alias);

        if(!isAliased && aggregation != Aggregation.None && aggregation != Aggregation.Count && aggregation != Aggregation.Array
                && field.type() == FieldType.Guid) {
            return new ToChar(result);
        }

        return result;
    }

    private SqlToken aggregate(SqlToken token, Aggregation aggregation) {
        switch(aggregation) {
        case Sum: {
            return new Sum(token);
        }
        case Max: {
            return new Max(token);
        }
        case Min: {
            return new Min(token);
        }
        case Average: {
            return new Average(token);
        }
        case Count: {
            return new Count(token);
        }
        case Array: {
            return new Array(token);
        }
        case Concat: {
            return new Concat(token);
        }
        default:
            return token;
        }
    }

    private void aggregate(Collection<IValue> fields) {
        for(IValue field : fields) {
            ((Field)field).setAggregated(true);
        }
    }

    @Override
    public FieldType type() {
        return field.type();
    }
}
