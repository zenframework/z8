package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.format.Format;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_date;

public class DatetimeField extends Field {
	public static class CLASS<T extends DatetimeField> extends Field.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(DatetimeField.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new DatetimeField(container);
		}
	}

	public DatetimeField(IObject container) {
		super(container);
		setDefault(date.Min);
		aggregation = Aggregation.Max;
		format = new string(Format.datetime);
	}

	public date z8_getDefault() {
		return (date)super.getDefault();
	}

	@Override
	public primary getDefault() {
		return (ApplicationServer.userEventsEnabled() && !changed()) ? z8_getDefault() : super.getDefault();
	}

	@Override
	public FieldType type() {
		return FieldType.Datetime;
	}

	@Override
	public String sqlType(DatabaseVendor vendor) {
		return type().vendorType(vendor);
	}

	public sql_date sql_date() {
		return new sql_date(new SqlField(this));
	}

	@Override
	public date get() {
		return z8_get();
	}

	@Override
	public primary parse(String value) {
		return new date(Long.parseLong(value));
	}

	public date z8_get() {
		return isArray() ? null : (date)internalGet();
	}

	public DatetimeField.CLASS<? extends DatetimeField> operatorAssign(date value) {
		set(value);
		return (DatetimeField.CLASS<?>)this.getCLASS();
	}
}
