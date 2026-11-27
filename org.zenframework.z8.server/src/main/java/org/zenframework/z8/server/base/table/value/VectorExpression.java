package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.vector;
import org.zenframework.z8.server.types.sql.sql_vector;

public class VectorExpression extends Expression {
	public static class CLASS<T extends VectorExpression> extends Expression.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(VectorExpression.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new VectorExpression(container);
		}
	}

	public VectorExpression(IObject container) {
		super(container);
		setDefault(vector.Zero);
		aggregation = Aggregation.None;
	}

	@Override
	public FieldType type() {
		return FieldType.Vector;
	}

	public sql_vector sql_vector() {
		return new sql_vector(new SqlField(this));
	}

	@Override
	public vector get() {
		return z8_get();
	}

	public vector z8_get() {
		return (vector)internalGet();
	}

	@Override
	public primary parse(String value) {
		return new vector(value);
	}

	public VectorExpression.CLASS<? extends VectorExpression> operatorAssign(vector value) {
		set(value);
		return (VectorExpression.CLASS<?>)this.getCLASS();
	}

	public VectorExpression.CLASS<? extends VectorExpression> operatorAssign(sql_vector expression) {
		setExpression(expression);
		return (VectorExpression.CLASS<?>)this.getCLASS();
	}
}
