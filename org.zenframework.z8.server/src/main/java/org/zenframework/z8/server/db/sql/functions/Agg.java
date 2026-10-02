package org.zenframework.z8.server.db.sql.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringJoiner;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.conversion.ToString;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public abstract class Agg extends SqlToken {
	protected SqlToken token;
	protected Collection<Field> orderBy = new ArrayList<Field>();
	protected boolean distinct;

	public Agg(SqlToken token, boolean distinct) {
		this.token = token;
		this.distinct = distinct;
	}

	public Agg(SqlToken token, boolean distinct, Collection<Field> orderBy) {
		this(token, distinct);
		this.orderBy.addAll(orderBy);
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		token.collectFields(fields);
		for(Field orderField: orderBy)
			new SqlField(orderField).collectFields(fields);
	}

	protected abstract SqlToken modifyOnDistinct(SqlToken token);
	protected abstract String getSql(DatabaseVendor vendor, String orderFields, String expression);

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) throws UnknownDatabaseException {
		if (token.type() == FieldType.Text) 
			token = new ToString(token); 
		String baseExpression = distinct ? token.format(vendor, options) : null;
		if (distinct) 
			token = modifyOnDistinct(token); 
		String expression = token.format(vendor, options);
		String orderFields= "";
		if (!orderBy.isEmpty()) {
			boolean aggregationWasEnabled = options.isAggregationEnabled();
			if(aggregationWasEnabled)
				options.disableAggregation();
			StringJoiner sj = new StringJoiner(", ");
			orderBy.forEach(field -> {
				String fieldExpression = new SqlField(field).format(vendor, options);
				if(distinct && !fieldExpression.equals(baseExpression))
					throw new RuntimeException("In aggregate functions with DISTINCT, the ORDER BY expressions must match the aggregated expression.");
				sj.add(fieldExpression + " " + field.sortDirection);
				});
			orderFields = sj.toString();
			if(aggregationWasEnabled)
				options.enableAggregation();
		}

		return getSql(vendor, orderFields, expression);
	}

	@Override
	public FieldType type() {
		return token.type();
	}
}
