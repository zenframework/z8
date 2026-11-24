package org.zenframework.z8.server.db.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;

public class SqlFieldList extends SqlToken {
	private final List<SqlField> fields = new ArrayList<SqlField>();

	public SqlFieldList(Collection<? extends Field> fields) {
		for (Field field : fields)
			this.fields.add(new SqlField(field));
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		for (SqlField field : this.fields)
			field.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		StringBuilder result = new StringBuilder();
		for (SqlField field : fields) {
			if (result.length() > 0)
				result.append(", ");
			result.append(field.format(vendor, options, false));
		}
		return result.toString();
	}

	@Override
	public FieldType type() {
		return FieldType.None;
	}
}
