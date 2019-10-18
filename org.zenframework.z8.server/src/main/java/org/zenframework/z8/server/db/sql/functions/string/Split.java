package org.zenframework.z8.server.db.sql.functions.string;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Split extends SqlToken {
	private final SqlToken str;
	private final SqlToken regexp;

	public Split(SqlToken str, SqlToken regexp) {
		this.str = str;
		this.regexp = regexp;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		str.collectFields(fields);
		regexp.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		StringBuilder result = new StringBuilder();

		if(vendor == DatabaseVendor.Postgres)
			result.append("regexp_split_to_array(").append(str.format(vendor, options)).append(',').append(regexp.format(vendor, options)).append(')');
		else
			throw new UnknownDatabaseException();

		return result.toString();
	}

	@Override
	public FieldType type() {
		return FieldType.String;
	}
}
