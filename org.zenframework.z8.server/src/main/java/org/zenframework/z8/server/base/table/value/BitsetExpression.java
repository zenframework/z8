package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bitset;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.sql.sql_bitset;

public class BitsetExpression extends Expression {
	public static class CLASS<T extends BitsetExpression> extends Expression.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(BitsetExpression.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new BitsetExpression(container);
		}
	}

	public BitsetExpression(IObject container) {
		super(container);
		setDefault(new bitset());
		aggregation = Aggregation.None;
	}

	@Override
	public FieldType type() {
		return FieldType.Bitset;
	}

	public sql_bitset sql_bitset() {
		return new sql_bitset(new SqlField(this));
	}

	@Override
	public bitset get() {
		return z8_get();
	}

	public bitset z8_get() {
		return (bitset) internalGet();
	}

	@Override
	public primary parse(String value) {
		return new bitset(value);
	}

	public BitsetExpression.CLASS<? extends BitsetExpression> operatorAssign(bitset value) {
		set(value);
		return (BitsetExpression.CLASS<?>)this.getCLASS();
	}

	public BitsetExpression.CLASS<? extends BitsetExpression> operatorAssign(sql_bitset expression) {
		setExpression(expression);
		return (BitsetExpression.CLASS<?>) this.getCLASS();
	}
}
