package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.geometry;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.sql.sql_geometry;

public class GeometryExpression extends Expression {
	public static class CLASS<T extends GeometryExpression> extends Expression.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(GeometryExpression.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new GeometryExpression(container);
		}
	}

	public integer srs = geometry.DefaultSRS;

	public GeometryExpression(IObject container) {
		super(container);

		setDefault(new geometry(srs.getInt()));
		aggregation = Aggregation.Array;
	}

	@Override
	public FieldType type() {
		return FieldType.Geometry;
	}

	public sql_geometry sql_geometry() {
		return new sql_geometry(new SqlField(this));
	}

	@Override
	public geometry get() {
		return z8_get();
	}

	@Override
	public primary parse(String value) {
		return new geometry(value);
	}

	public geometry z8_get() {
		return isArray() ? null : (geometry)internalGet();
	}

	public GeometryExpression.CLASS<? extends GeometryExpression> operatorAssign(geometry value) {
		set(value);
		return (GeometryExpression.CLASS<?>)this.getCLASS();
	}
	
	public GeometryExpression.CLASS<? extends GeometryExpression> operatorAssign(sql_geometry expression) {
		setExpression(expression);
		return (GeometryExpression.CLASS<?>)this.getCLASS();
	}
}
