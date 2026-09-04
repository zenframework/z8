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
		boolean asJson = options.isOrderBy();
		String expression = token.format(vendor, options, isLogicalContext);

		switch (vendor) {
		case Postgres:
		case Oracle:
		case H2:
			return "DISTINCT " + expression;
		case SqlServer:
			if (asJson)
				return "DISTINCT '\"' + " + expression + " + '\"'";
			else
				return "DISTINCT " + expression;
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return token.type();
	}

}
