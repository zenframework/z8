package org.zenframework.z8.server.db.sql.functions;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Distinct extends SqlToken {
	private SqlToken token;

	public Distinct(SqlToken token) {
		this.token = token;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		token.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean isLogicalContext) {
		StringBuilder result = new StringBuilder(1024);
		if (vendor == DatabaseVendor.Postgres)
			result.append("distinct ").append(token.format(vendor, options, isLogicalContext));
		else
			throw new UnknownDatabaseException();
		return result.toString();
	}

	@Override
	public FieldType type() {
		return token.type();
	}

}
