package org.zenframework.z8.server.db.sql.functions;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.conversion.ToString;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Array extends SqlToken {
	private SqlToken token;
	private boolean distinct;

	public Array(SqlToken token, boolean distinct) {
		this.token = token;
		this.distinct = distinct;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		token.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) throws UnknownDatabaseException {
		String result = "";
		switch(vendor) {
		case Oracle:
			result = "collect";
			break;
		case Postgres:
			result = options.isOrderBy() ? "array_agg" : "json_agg";
			if (token.type() == FieldType.Text)
				token = new ToString(token);
			if (distinct)
				token = new Distinct(token);
			break;
		default:
			throw new UnknownDatabaseException();
		}
		return result + '('  + token.format(vendor, options) + ')';
	}

	@Override
	public FieldType type() {
		return token.type();
	}
}
