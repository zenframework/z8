package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.functions.conversion.ToString;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.sql.sql_string;

public class TextField extends StringField {
	public static class CLASS<T extends TextField> extends StringField.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(TextField.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new TextField(container);
		}
	}

	public bool html;
	public bool singleLine;

	public TextField(IObject container) {
		super(container);
		length = new integer(0);
	}

	@Override
	public FieldType type() {
		return FieldType.Text;
	}

	@Override
	public FieldType metaType() {
		return bool.True.equals(singleLine) ? FieldType.String : super.metaType();
	}

	@Override
	public String sqlType(DatabaseVendor vendor) {
		String name = type().vendorType(vendor);

		if(vendor == DatabaseVendor.SqlServer)
			return name + "(MAX)";
		return name;
	}

	@Override
	public sql_string sql_string() {
		return new sql_string(new ToString(this));
	}

	@Override
	public void writeMeta(JsonWriter writer, Query query, Query context) {
		super.writeMeta(writer, query, context);
		writer.writeProperty(Json.html, html);
	}
}
