package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.format.Format;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_integer;

public class IntegerExpression extends Expression {
	public static class CLASS<T extends IntegerExpression> extends Expression.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(IntegerExpression.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new IntegerExpression(container);
		}
	}

	public IntegerExpression(IObject container) {
		super(container);

		format = new string(Format.integer);
		aggregation = Aggregation.Sum;
		setDefault(integer.Zero);
	}

	public IntegerExpression() {
		this(null);
	}

	@Override
	public FieldType type() {
		return FieldType.Integer;
	}

	public sql_integer sql_int() {
		return new sql_integer(new SqlField(this));
	}

	@Override
	public primary get() {
		return z8_get();
	}

	public integer z8_get() {
		return (integer)internalGet();
	}

	@Override
	public primary parse(String value) {
		return new integer(value);
	}

	public IntegerExpression.CLASS<? extends IntegerExpression> operatorAssign(integer value) {
		set(value);
		return (IntegerExpression.CLASS<?>)this.getCLASS();
	}
}
