package org.zenframework.z8.server.db.sql.functions;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlStringToken;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Aggregate extends SqlToken {
    private SqlToken parameter;

    public Aggregate(String parameter, Aggregation aggregation) {
        this(new SqlStringToken(parameter), aggregation);
    }

    public Aggregate(SqlToken parameter, Aggregation aggregation) {
        if(aggregation == Aggregation.None) {
            this.parameter = parameter;
        }
        else if(aggregation == Aggregation.Sum) {
            this.parameter = new Sum(parameter);
        }
        else if(aggregation == Aggregation.Min) {
            this.parameter = new Min(parameter);
        }
        else if(aggregation == Aggregation.Max) {
            this.parameter = new Max(parameter);
        }
        else if(aggregation == Aggregation.Average) {
            this.parameter = new Average(parameter);
        }
        else if(aggregation == Aggregation.Count) {
            this.parameter = new Count(parameter);
        }
        else {
            assert (false);
        }
    }

    @Override
    public void collectFields(Collection<IValue> fields) {
        parameter.collectFields(fields);
    }

    @Override
    public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext)
            throws UnknownDatabaseException {
        return parameter.format(vendor, options, logicalContext);
    }

    @Override
    public FieldType type() {
        return parameter.type();
    }

}
