package org.zenframework.z8.server.db.sql.functions.string;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.conversion.ToString;

public class Lower extends StringFunction {
	private SqlToken string;

	public Lower(Field field) {
		this(new SqlField(field));
	}

	public Lower(SqlToken value) {
		this.string = value;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		string.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		SqlToken token = string.type() != FieldType.String ? new ToString(string) : string;
		return "LOWER(" + token.format(vendor, options) + ")";
	}

	@Override
	public FieldType type() {
		return FieldType.String;
	}
}
