package org.zenframework.z8.server.db.sql.functions.numeric;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Ln extends SqlToken {
	private SqlToken token;

	public Ln(SqlToken token) {
		this.token = token;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		token.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(vendor) {
		case Oracle:
			return "LN(" + token.format(vendor, options) + ")";
		case SqlServer:
			return "LOG(" + token.format(vendor, options) + ")";
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Decimal;
	}
}
