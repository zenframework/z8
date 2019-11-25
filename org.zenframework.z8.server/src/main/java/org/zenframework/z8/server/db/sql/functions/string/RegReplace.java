package org.zenframework.z8.server.db.sql.functions.string;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class RegReplace extends StringFunction {
	private SqlToken string;
	private SqlToken pattern;
	private SqlToken replacement;
	private SqlToken flags;

	public RegReplace(SqlToken string, SqlToken pattern, SqlToken replacement, SqlToken flags) {
		this.string = string;
		this.pattern = pattern;
		this.replacement = replacement;
		this.flags = flags;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		string.collectFields(fields);
		pattern.collectFields(fields);
		replacement.collectFields(fields);
		if (flags != null)
			flags.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		StringBuilder str = new StringBuilder(1024);

		switch(vendor) {
		case Oracle:
		case SqlServer:
		case Postgres:
			str.append("regexp_replace(").append(string.format(vendor, options)).append(", ").append(pattern.format(vendor, options)).append(", ").append(replacement.format(vendor, options));
			if (flags != null)
				str.append(", ").append(flags.format(vendor, options));
			str.append(")");
			return str.toString();
		default:
			throw new UnknownDatabaseException();
		}
	}
}
