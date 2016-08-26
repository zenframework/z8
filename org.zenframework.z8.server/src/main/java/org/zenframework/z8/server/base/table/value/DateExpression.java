package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.format.Format;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.string;

public class DateExpression extends DatetimeExpression {
	public static class CLASS<T extends DateExpression> extends Expression.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(DateExpression.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new DateExpression(container);
		}
	}

	public DateExpression() {
		this(null);
	}

	public DateExpression(IObject container) {
		super(container);
		format = new string(Format.date);
		stretch = new bool(false);
	}

	@Override
	public FieldType type() {
		return FieldType.Date;
	}
}
