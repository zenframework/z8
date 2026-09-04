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

public class JsonAgg extends SqlToken {
	private SqlToken token;
	private Collection<Field> orderBy = new ArrayList<Field>();
	private boolean distinct;

	public JsonAgg(SqlToken token, boolean distinct) {
		this.token = token;
		this.distinct = distinct;
	}

	public JsonAgg(SqlToken token, boolean distinct, Collection<Field> orderBy) {
		this(token, distinct);
		this.orderBy.addAll(orderBy);
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		token.collectFields(fields);
		for(Field orderField: orderBy)
			new SqlField(orderField).collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) throws UnknownDatabaseException {
		if (token.type() == FieldType.Text) 
			token = new ToString(token); 
		if (distinct) 
			token = new JsonDistinct(token); 
		String expression = token.format(vendor, options);
		String orderFields= "";
		if (!orderBy.isEmpty()) {
			boolean aggregationWasEnabled = options.isAggregationEnabled();
			if(aggregationWasEnabled)
				options.disableAggregation();
			StringJoiner sj = new StringJoiner(", ");
			orderBy.forEach(field -> sj.add(new SqlField(field).format(vendor, options) + " " + field.sortDirection));
			orderFields = sj.toString();
			if(aggregationWasEnabled)
				options.enableAggregation();
		}

		switch(vendor) {
		case Oracle:
		case H2: {
			String orderSql = orderFields.isEmpty() ? "" : " ORDER BY " + orderFields;
			return "JSON_ARRAYAGG(" + expression + orderSql + ")";
		}
		case Postgres: {
			String orderSql = orderFields.isEmpty() ? "" : " ORDER BY " + orderFields;
			return "json_agg(" + expression + orderSql + ")";
		}
		case SqlServer: {
			String orderSql = orderFields.isEmpty() ? "" : " WITHIN GROUP (ORDER BY " + orderFields + ")";
			if (distinct)
				return "'[' + STRING_AGG(" + expression + ", ',')" + orderSql + " + ']'";
			else
				return "'[' + STRING_AGG('\"' + " + expression + " + '\"', ',')" + orderSql + " + ']'";
		}
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return token.type();
	}
}
