package org.zenframework.z8.server.db.sql.functions.conversion;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IValue;
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
	public void collectFields(Collection<IValue> fields) {
		value.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		FieldType type = value.type();

		switch(vendor) {
		case Oracle:
			switch(type) {
			case Guid:
				return new GuidToString(value).format(vendor, options);
			case Attachments:
			case File:
			case Text:
				return "UTL_RAW.CAST_TO_NVARCHAR2(" + value.format(vendor, options) + ")";
			default:
				return value.format(vendor, options);
			}

		case Postgres:
			if (type == FieldType.Guid)
				return new GuidToString(value).format(vendor, options);
			if (type == FieldType.Date || type == FieldType.Datetime)
				return new DateToString(value).format(vendor, options);
			if (type == FieldType.Attachments || type == FieldType.File || type == FieldType.Text)
				return "CONVERT_FROM(" + value.format(vendor, options) + ", 'UTF8')";
			if (type == FieldType.Integer || type == FieldType.Decimal
					|| value instanceof SqlField && (((SqlField) value).aggregation() == Aggregation.Array
							|| ((SqlField) value).aggregation() == Aggregation.Distinct))
				return "(" + value.format(vendor, options) + ")::text";
			return value.format(vendor, options);

		case SqlServer:
			return "Cast(" + value.format(vendor, options) + " as nvarchar(max))";

		case H2:
			return value.format(vendor, options);

		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.String;
	}
}
