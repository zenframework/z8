package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.sql.sql_binary;

public class BinaryExpression extends Expression {
	public static class CLASS<T extends BinaryExpression> extends Expression.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(BinaryExpression.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new BinaryExpression(container);
		}
	}

	public BinaryExpression(IObject container) {
		super(container);

		setDefault(new binary());
	}

	@Override
	public FieldType type() {
		return FieldType.Binary;
	}

	public sql_binary sql_binary() {
		return new sql_binary(new SqlField(this));
	}

	@Override
	public primary get() {
		return z8_get();
	}

	public binary z8_get() {
		return (binary)internalGet();
	}

	public BinaryExpression.CLASS<? extends BinaryExpression> operatorAssign(sql_binary expression) {
		setExpression(expression);
		return (BinaryExpression.CLASS<?>)this.getCLASS();
	}
}
