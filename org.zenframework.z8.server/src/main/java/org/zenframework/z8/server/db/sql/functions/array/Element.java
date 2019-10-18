package org.zenframework.z8.server.db.sql.functions.array;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Element extends SqlToken {
	private SqlToken array;
	private SqlToken index;

	public Element(SqlToken array, SqlToken index) {
		this.array = array;
		this.index = index;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		this.array.collectFields(fields);
		this.index.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		StringBuilder result = new StringBuilder(1024);

		if (vendor == DatabaseVendor.Postgres)
			result.append('(').append(array.format(vendor, options, logicalContext)).append(")[").append(index.format(vendor, options, logicalContext)).append(']');
		else
			throw new UnknownDatabaseException();

		return result.toString();
	}

	@Override
	public FieldType type() {
		return array.type();
	}
}
