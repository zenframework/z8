package org.zenframework.z8.server.db.sql.functions.array;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Contains extends SqlToken {
	private SqlToken array;
	private SqlToken value;

	public Contains(SqlToken array, SqlToken value) {
		this.array = array;
		this.value = value;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		this.array.collectFields(fields);
		this.value.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		StringBuilder result = new StringBuilder(1024);

		if (vendor == DatabaseVendor.Postgres)
			result.append(value.format(vendor, options, logicalContext)).append("=ANY(").append(array.format(vendor, options, logicalContext)).append(')');
		else
			throw new UnknownDatabaseException();

		return result.toString();
	}

	@Override
	public FieldType type() {
		return FieldType.Boolean;
	}
}
