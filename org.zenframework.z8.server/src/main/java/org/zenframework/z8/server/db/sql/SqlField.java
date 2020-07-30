package org.zenframework.z8.server.db.sql;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.base.table.value.Expression;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
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
	public void collectFields(Collection<IField> fields) {
		if(field.isExpression()) {
			Expression expression = (Expression)field;
			SqlToken token = expression.expression();

			if(token != null)
				token.collectFields(fields);
		} else
			fields.add(field);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) throws UnknownDatabaseException {
		Aggregation aggregation = field.getAggregation();

		if(aggregation != Aggregation.None)
			options.disableAggregation();

		SqlToken token = getToken(vendor, options, logicalContext);

		if(aggregation != Aggregation.None)
			options.enableAggregation();

		return aggregate(token, options.isAggregationEnabled() ? aggregation : Aggregation.None).format(vendor, options, logicalContext);
	}

	private SqlToken getToken(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		FieldType type = field.type();

		String alias = field.format(vendor, options);
		// and field like '' /*setExpression(field.like(''))*/
		// and field like '' /*setExpression(field.like('').IIF(1, 0) == 1)*/
		if(logicalContext && type == FieldType.Boolean/* && !(field instanceof Expression)*/)
			alias += "=1";

		return new SqlStringToken(alias, type);
	}

	public SqlToken aggregate(SqlToken token, Aggregation aggregation) {
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
			return new Array(token, false);
		case Distinct:
			return new Array(token, true);
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

	public Aggregation aggregation() {
		return field.aggregation;
	}
}
