package org.zenframework.z8.server.db.sql.functions.string;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class RegLike extends SqlToken {
	private SqlToken string;
	private SqlToken pattern;

	public RegLike(SqlToken string, SqlToken pattern) {
		this.string = string;
		this.pattern = pattern;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		string.collectFields(fields);
		pattern.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		if(vendor == DatabaseVendor.Postgres)
			return string.format(vendor, options) + " ~ " + pattern.format(vendor, options);
		else
			throw new UnknownDatabaseException();
	}

	@Override
	public FieldType type() {
		return FieldType.Integer;
	}
}
