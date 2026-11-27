package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.vector;
import org.zenframework.z8.server.types.sql.sql_vector;

public class VectorField extends Field {
	static public int DefaultLength = 2;

	public static class CLASS<T extends VectorField> extends Field.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(VectorField.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new VectorField(container);
		}
	}

	public VectorField(IObject container) {
		super(container);
		length = new integer(DefaultLength);
		aggregation = Aggregation.None;
	}

	@Override
	public String sqlType(DatabaseVendor vendor) {
		return vendor.dialect().formatSqlType(type(), length.get());
	}

	@Override
	public primary getDefaultValue() {
		if (defaultValue == null)
			defaultValue = new vector(length.getInt());
		return defaultValue;
	}

	public vector z8_getDefault() {
		return (vector) super.z8_getDefault();
	}

	@Override
	public FieldType type() {
		return FieldType.Vector;
	}

	public sql_vector sql_vector() {
		return new sql_vector(new SqlField(this));
	}

	public void set(String value) {
		set(new string(value));
	}

	@Override
	public vector get() {
		return z8_get();
	}

	@Override
	public primary parse(String value) {
		return new vector(value);
	}

	public vector z8_get() {
		return (vector)internalGet();
	}

	public VectorField.CLASS<? extends VectorField> operatorAssign(vector value) {
		set(value);
		return (VectorField.CLASS<?>)this.getCLASS();
	}

	public VectorField.CLASS<? extends VectorField> operatorAssign(RCollection<primary> value) {
		return operatorAssign(new vector(value));
	}
}
