package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.sql.sql_guid;

public class GuidExpression extends Expression {
	public static class CLASS<T extends GuidExpression> extends Expression.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(GuidExpression.class);
			setSystem(true);
		}

		@Override
		public Object newObject(IObject container) {
			return new GuidExpression(container);
		}
	}

	public GuidExpression(IObject container) {
		super(container);
		setDefault(new guid());
		aggregation = Aggregation.Max;
	}

	@Override
	public FieldType type() {
		return FieldType.Guid;
	}

	public sql_guid sql_guid() {
		return new sql_guid(new SqlField(this));
	}

	@Override
	public primary get() {
		return z8_get();
	}

	@Override
	public primary parse(String value) {
		return new guid(value);
	}

	public guid z8_get() {
		return (guid)internalGet();
	}

	public GuidExpression.CLASS<? extends GuidExpression> operatorAssign(guid value) {
		set(value);
		return (GuidExpression.CLASS<?>)this.getCLASS();
	}
}
