package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.sql.sql_datespan;

public class DatespanField extends Field {
	public static class CLASS<T extends DatespanField> extends Field.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(DatespanField.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new DatespanField(container);
		}
	}

	public DatespanField(IObject _container) {
		super(_container);
		setDefault(new datespan());
	}

	public datespan z8_getDefault() {
		return (datespan)super.getDefault();
	}

	@Override
	public primary getDefault() {
		return (ApplicationServer.events() && !changed()) ? z8_getDefault() : super.getDefault();
	}

	@Override
	public FieldType type() {
		return FieldType.Datespan;
	}

	@Override
	public String sqlType(DatabaseVendor vendor) {
		String name = type().vendorType(vendor);

		if(vendor == DatabaseVendor.Oracle) {
			return name + "(19, 0)";
		}
		return name;
	}

	public sql_datespan sql_datespan() {
		return new sql_datespan(new SqlField(this));
	}

	@Override
	public primary get() {
		return z8_get();
	}

	@Override
	public primary parse(String value) {
		return new datespan(Long.parseLong(value));
	}

	public datespan z8_get() {
		return (datespan)internalGet();
	}

	public DatespanField.CLASS<? extends DatespanField> operatorAssign(datespan value) {
		set(value);
		return (DatespanField.CLASS<?>)this.getCLASS();
	}
}
