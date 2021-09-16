package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.functions.If;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.sql.sql_bool;
import org.zenframework.z8.server.types.sql.sql_date;
import org.zenframework.z8.server.types.sql.sql_datespan;
import org.zenframework.z8.server.types.sql.sql_decimal;
import org.zenframework.z8.server.types.sql.sql_geometry;
import org.zenframework.z8.server.types.sql.sql_guid;
import org.zenframework.z8.server.types.sql.sql_integer;
import org.zenframework.z8.server.types.sql.sql_string;

public class BoolField extends Field {
	public static class CLASS<T extends BoolField> extends Field.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(BoolField.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new BoolField(container);
		}
	}

	public BoolField(IObject container) {
		super(container);
		setDefault(bool.False);
		width = new integer(50);
		aggregation = Aggregation.Max;
	}

	@Override
	public FieldType type() {
		return FieldType.Boolean;
	}

	@Override
	public String sqlType(DatabaseVendor vendor) {
		String name = type().vendorType(vendor);

		if(vendor == DatabaseVendor.Oracle) {
			return name + "(1)";
		}
		return name;
	}

	public bool z8_getDefault() {
		return (bool)super.getDefault();
	}

	@Override
	public primary getDefault() {
		return (ApplicationServer.userEventsEnabled() && !changed()) ? z8_getDefault() : super.getDefault();
	}

	public sql_bool sql_bool() {
		return new sql_bool(new SqlField(this));
	}

	@Override
	public primary get() {
		return z8_get();
	}

	@Override
	public primary parse(String value) {
		return new bool(value);
	}

	public bool z8_get() {
		return isArray() ? null : (bool)internalGet();
	}

	public BoolField.CLASS<? extends BoolField> operatorAssign(bool value) {
		set(value);
		return (BoolField.CLASS<?>)this.getCLASS();
	}

	public sql_bool z8_IIF(sql_bool yes, sql_bool no) {
		return new sql_bool(new If(this, yes, no));
	}

	public sql_datespan z8_IIF(sql_datespan yes, sql_datespan no) {
		return new sql_datespan(new If(this, yes, no));
	}

	public sql_date z8_IIF(sql_date yes, sql_date no) {
		return new sql_date(new If(this, yes, no));
	}

	public sql_decimal z8_IIF(sql_decimal yes, sql_decimal no) {
		return new sql_decimal(new If(this, yes, no));
	}

	public sql_guid z8_IIF(sql_guid yes, sql_guid no) {
		return new sql_guid(new If(this, yes, no));
	}

	public sql_integer z8_IIF(sql_integer yes, sql_integer no) {
		return new sql_integer(new If(this, yes, no));
	}

	public sql_string z8_IIF(sql_string yes, sql_string no) {
		return new sql_string(new If(this, yes, no));
	}

	public sql_geometry z8_IIF(sql_geometry yes, sql_geometry no) {
		return new sql_geometry(new If(this, yes, no));
	}

}
