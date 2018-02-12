package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.sql.sql_guid;

public class GuidField extends Field {
	public static class CLASS<T extends GuidField> extends Field.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(GuidField.class);
			setSystem(true);
		}

		@Override
		public Object newObject(IObject container) {
			return new GuidField(container);
		}
	}

	public GuidField(IObject container) {
		super(container);
		setDefault(new guid());
		aggregation = Aggregation.Max;
	}

	public guid z8_getDefault() {
		return (guid)super.getDefault();
	}

	@Override
	public primary getDefault() {
		return (ApplicationServer.events() && !changed()) ? z8_getDefault() : super.getDefault();
	}

	@Override
	public FieldType type() {
		return FieldType.Guid;
	}

	@Override
	public String sqlType(DatabaseVendor vendor) {
		String name = type().vendorType(vendor);

		if(vendor == DatabaseVendor.Oracle)
			return name + "(16)";

		return name;
	}

	public sql_guid sql_guid() {
		return new sql_guid(new SqlField(this));
	}

	@Override
	public primary get() {
		return z8_get();
	}

	public guid z8_get() {
		return (guid)internalGet();
	}

	@Override
	public primary parse(String value) {
		return new guid(value);
	}

	public GuidField.CLASS<? extends GuidField> operatorAssign(guid value) {
		set(value);
		return (GuidField.CLASS<?>)this.getCLASS();
	}
}
