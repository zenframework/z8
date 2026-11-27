package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bitset;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_bitset;

public class BitsetField extends Field {
	private static final int DefaultLength = 64;

	public static class CLASS<T extends BitsetField> extends Field.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(BitsetField.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new BitsetField(container);
		}
	}

	public BitsetField(IObject container) {
		super(container);
		length = new integer(DefaultLength);
		aggregation = Aggregation.None;
	}

	@Override
	public primary getDefaultValue() {
		if(defaultValue == null)
			defaultValue = new bitset(length.getInt());
		return defaultValue;
	}

	public bitset z8_getDefault() {
		return (bitset) super.z8_getDefault();
	}

	@Override
	public FieldType type() {
		return FieldType.Bitset;
	}

	@Override
	public int size() {
		return length.getInt();
	}

	@Override
	public String sqlType(DatabaseVendor vendor) {
		return vendor.dialect().formatSqlType(type(), length);
	}

	@Override
	public String sqlPlaceholder(DatabaseVendor vendor) {
		return vendor.dialect().formatSqlPlaceholder(type(), length);
	}

	public sql_bitset sql_bitset() {
		return new sql_bitset(new SqlField(this));
	}

	public void set(String value) {
		set(new string(value));
	}

	@Override
	public bitset get() {
		return z8_get();
	}

	@Override
	public primary parse(String value) {
		return new bitset(value);
	}

	public bitset z8_get() {
		return (bitset) internalGet();
	}

	public BitsetField.CLASS<? extends BitsetField> operatorAssign(bitset value) {
		set(value);
		return (BitsetField.CLASS<?>) this.getCLASS();
	}
}
