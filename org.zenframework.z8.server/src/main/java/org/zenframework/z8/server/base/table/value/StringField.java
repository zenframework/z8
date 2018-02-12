package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_string;

public class StringField extends Field {
	static public int DefaultLength = 30;

	public static class CLASS<T extends StringField> extends Field.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(StringField.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new StringField(container);
		}
	}

	public StringField(IObject container) {
		super(container);
		setDefault(new string(""));
		length = new integer(DefaultLength);
		aggregation = Aggregation.Max;
	}

	public string z8_getDefault() {
		return (string)super.getDefault();
	}

	@Override
	public primary getDefault() {
		return (ApplicationServer.events() && !changed()) ? z8_getDefault() : super.getDefault();
	}

	@Override
	public FieldType type() {
		return FieldType.String;
	}

	@Override
	public int size() {
		return length.getInt();
	}

	@Override
	public String sqlType(DatabaseVendor vendor) {
		String name = type().vendorType(vendor);
		return name + "(" + length.get() + ")";
	}

	public sql_string sql_string() {
		return new sql_string(new SqlField(this));
	}

	public void set(String value) {
		set(new string(value));
	}

	@Override
	public primary get() {
		return z8_get();
	}

	@Override
	public primary parse(String value) {
		return new string(value);
	}

	public string z8_get() {
		return (string)internalGet();
	}

	public StringField.CLASS<? extends StringField> operatorAssign(string value) {
		set(value);
		return (StringField.CLASS<?>)this.getCLASS();
	}

	@Override
	public void writeMeta(JsonWriter writer, Query query, Query context) {
		super.writeMeta(writer, query, context);

		writer.writeProperty(Json.length, length);
	}
}
