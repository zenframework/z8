package org.zenframework.z8.server.db.sql.functions.vector;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Fill extends SqlToken {

	private final SqlToken size, value;

	public Fill(SqlToken size, SqlToken value) {
		this.size = size;
		this.value = value;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		size.collectFields(fields);
		value.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch (vendor) {
		case Postgres:
			return new StringBuilder(1024).append("array_fill(").append(value.format(vendor, options)).append(", ARRAY[")
					.append(size.format(vendor, options)).append("])").toString();
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Vector;
	}

}
