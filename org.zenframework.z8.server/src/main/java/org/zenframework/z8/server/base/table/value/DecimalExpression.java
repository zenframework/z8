package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.format.Format;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_decimal;

public class DecimalExpression extends Expression {
	public static class CLASS<T extends DecimalExpression> extends Expression.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(DecimalExpression.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new DecimalExpression(container);
		}
	}

	public DecimalExpression(IObject container) {
		super(container);
		format = new string(Format.decimal);
		aggregation = Aggregation.Sum;

		setDefault(decimal.Zero);
	}

	@Override
	public FieldType type() {
		return FieldType.Decimal;
	}

	public sql_decimal sql_decimal() {
		return new sql_decimal(new SqlField(this));
	}

	@Override
	public decimal get() {
		return z8_get();
	}

	public decimal z8_get() {
		return isArray() ? null : (decimal)internalGet();
	}

	@Override
	public primary parse(String value) {
		return new decimal(value);
	}

	public DecimalExpression.CLASS<? extends DecimalExpression> operatorAssign(decimal value) {
		set(value);
		return (DecimalExpression.CLASS<?>)this.getCLASS();
	}

	public DecimalExpression.CLASS<? extends DecimalExpression> operatorAssign(sql_decimal expression) {
		setExpression(expression);
		return (DecimalExpression.CLASS<?>)this.getCLASS();
	}
}
