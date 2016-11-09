package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.sql.sql_datespan;

public class DatespanExpression extends Expression {
	public static class CLASS<T extends DatespanExpression> extends Expression.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(DatespanExpression.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new DatespanExpression(container);
		}
	}

	public DatespanExpression(IObject container) {
		super(container);

		setDefault(new datespan());
	}

	@Override
	public FieldType type() {
		return FieldType.Datespan;
	}

	public sql_datespan sql_datespan() {
		return new sql_datespan(new SqlField(this));
	}

	@Override
	public primary get() {
		return z8_get();
	}

	public datespan z8_get() {
		return (datespan)internalGet();
	}

	public DatespanExpression.CLASS<? extends DatespanExpression> operatorAssign(datespan value) {
		set(value);
		return (DatespanExpression.CLASS<?>)this.getCLASS();
	}
}
