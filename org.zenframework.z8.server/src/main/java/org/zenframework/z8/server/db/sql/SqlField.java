package org.zenframework.z8.server.db.sql;

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
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class SqlField extends SqlToken {
	private Field field = null;

	public SqlField(Field field) {
		this.field = field;
	}

	@Override
	public void collectFields(Collection<IValue> fields) {
		if(field instanceof Expression) {
			Expression expression = (Expression)field;
			SqlToken token = expression.expression();

			if(token != null)
				token.collectFields(fields);
		} else
			fields.add(field);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) throws UnknownDatabaseException {
		Aggregation aggregation = options.isAggregationEnabled() ? field.getAggregation() : Aggregation.None;

		options.disableAggregation();
		SqlToken token = getToken(vendor, options, logicalContext, aggregation);
		options.enableAggregation();

		return aggregate(token, aggregation).format(vendor, options, logicalContext);
	}

	private SqlToken getToken(DatabaseVendor vendor, FormatOptions options, boolean logicalContext, Aggregation aggregation) {
		FieldType type = field.type();

		String alias = field.format(vendor, options);
		if(type == FieldType.Boolean)
			alias += logicalContext ? "=1" : "";

		return new SqlStringToken(alias, type);
	}

	private SqlToken aggregate(SqlToken token, Aggregation aggregation) {
		switch(aggregation) {
		case Sum:
			return new Sum(token);
		case Max:
			return new Max(token);
		case Min:
			return new Min(token);
		case Average:
			return new Average(token);
		case Count:
			return new Count(token);
		case Array:
			return new Array(token);
		case Concat:
			return new Concat(token);
		default:
			return token;
		}
	}

	@Override
	public FieldType type() {
		return field.type();
	}
}
