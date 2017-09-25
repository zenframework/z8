package org.zenframework.z8.server.db.sql.functions.conversion;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class ToString extends SqlToken {
	private SqlToken value;

	public ToString(Field field) {
		this(new SqlField(field));
	}

	public ToString(SqlToken value) {
		this.value = value;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		value.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(vendor) {
		case Oracle:
			return "TO_NCHAR(" + value.format(vendor, options) + ")";
		case Postgres:
			FieldType type = value.type();
			if(type == FieldType.Guid)
				return new GuidToString(value).format(vendor, options);
			else if(type == FieldType.Date || type == FieldType.Datetime)
				return new DateToString(value).format(vendor, options);
			else
				return "CONVERT_FROM(" + value.format(vendor, options) + ", 'UTF8')";
		case SqlServer:
			return "Cast(" + value.format(vendor, options) + " as nvarchar(max))";
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.String;
	}
}
