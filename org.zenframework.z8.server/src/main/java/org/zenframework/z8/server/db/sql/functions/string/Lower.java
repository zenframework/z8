package org.zenframework.z8.server.db.sql.functions.string;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.conversion.ToChar;

public class Lower extends StringFunction {
	private SqlToken value;

	public Lower(Field field) {
		this(new SqlField(field));
	}

	public Lower(SqlToken value) {
		this.value = value;
	}

	@Override
	public void collectFields(Collection<IValue> fields) {
		value.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		SqlToken token = value.type() != FieldType.String ? new ToChar(value) : value;
		return "LOWER(" + token.format(vendor, options) + ")";
	}

	@Override
	public FieldType type() {
		return FieldType.String;
	}
}
