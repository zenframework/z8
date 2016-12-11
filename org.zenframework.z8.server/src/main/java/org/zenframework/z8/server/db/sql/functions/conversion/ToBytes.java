package org.zenframework.z8.server.db.sql.functions.conversion;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class ToBytes extends SqlToken {
	private SqlToken value;

	public ToBytes(Field field) {
		this(new SqlField(field));
	}

	public ToBytes(SqlToken value) {
		this.value = value;
	}

	@Override
	public void collectFields(Collection<IValue> fields) {
		value.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(vendor) {
		case Postgres:
			return "CONVERT_TO(" + value.format(vendor, options) + ", 'UTF8')";
		case Oracle:
		case SqlServer:
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Binary;
	}
}
