package org.zenframework.z8.server.base.model.sql;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.Expression;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.functions.date.ServerTime;
import org.zenframework.z8.server.types.sql.sql_integer;

public class FramedSelect extends Select {
	int start = 0;
	int limit = -1;

	public FramedSelect(Select select, int start, int limit) {
		super(select);

		this.start = start;
		this.limit = limit;
	}

	@Override
	protected String sql(FormatOptions options) {
		DatabaseVendor vendor = database().vendor();

		if(vendor != DatabaseVendor.Postgres) {
			Collection<Field> orderByFields = getOrderBy();

			Expression frameExpression = getFrameExpression(options, orderByFields, isGrouped());
			getFields().add(frameExpression);

			setOrderBy(null);

			setSubselect(new Select(this));

			setRootQuery(null);
			setLinks(null);

			setGroupBy(null);
			setWhere(getFrameWhere(frameExpression, start, limit));
			setHaving(null);

			return super.sql(options);
		} else {
			String sql = super.sql(options);

			sql += "\nlimit " + (limit != -1 ? limit : "all") + " offset " + start;
			return sql;
		}
	}

	private Expression getFrameExpression(FormatOptions options, Collection<Field> orderBy, boolean grouped) {
		return new Expression(new SqlRowNumber(orderBy, grouped), FieldType.Integer);
	}

	private SqlToken getFrameWhere(Expression frameExpression, int start, int limit) {
		SqlToken left = new Rel(frameExpression, Operation.GE, new sql_integer(start));
		SqlToken right = new Rel(frameExpression, Operation.LT, new sql_integer(start + limit));
		return limit > 0 ? new And(left, right) : left;
	}
}

class SqlRowNumber extends SqlToken {
	private Collection<Field> orderBy = null;
	private boolean grouped = false;

	public SqlRowNumber(Collection<Field> orderBy, boolean grouped) {
		this.orderBy = orderBy;
		this.grouped = grouped;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		if(orderBy != null)
			fields.addAll(orderBy);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean isLogicalContext) {
		String expression = "row_number() over (order by ";

		if(orderBy == null || orderBy.isEmpty()) {
			expression += new ServerTime().format(vendor);
		} else {
			String result = "";

			for(Field field : orderBy) {
				String name = grouped ? new SqlField(field).format(vendor, options) : field.format(vendor, options);
				result += (result.isEmpty() ? "" : ", ") + name + " " + field.sortDirection;
			}

			expression += result;
		}

		expression += ")";
		return expression;
	}

	@Override
	public FieldType type() {
		return FieldType.Integer;
	}
}
