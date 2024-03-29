package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.primary;

public class Expression extends Field {
	public static class CLASS<T extends Expression> extends Field.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Expression.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Expression(container);
		}
	}

	private SqlToken expression = null;
	private FieldType expressionType = FieldType.None;

	public Expression(IObject container) {
		super(container);
	}

	public Expression(SqlToken expression, FieldType expressionType) {
		this(null);
		this.expression = expression;
		this.expressionType = expressionType;
	}

	@Override
	public FieldType type() {
		return expression != null ? expressionType : FieldType.None;
	}

	@Override
	public String sqlType(DatabaseVendor vendor) {
		return null;
	}

	@Override
	public boolean isExpression() {
		return true;
	}

	@Override
	public boolean wasNull() {
		return false;
	}

	final public SqlToken expression() {
		if(expression == null) {
			expression = z8_expression();
		}

		return expression;
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options) {
		String alias = options.getFieldAlias(this);

		if(alias == null)
			return expression().format(vendor, options, false);

		return alias;
	}

	@Override
	public primary get() {
		return internalGet();
	}

	@Override
	public primary parse(String value) {
		throw new UnsupportedOperationException();
	}

	protected SqlToken z8_expression() {
		return new SqlConst(getDefault());
	}

	public void setExpression(SqlToken token) {
		this.expression = token;
	}

	public void z8_setExpression(SqlToken token) {
		setExpression(token);
	}
}
