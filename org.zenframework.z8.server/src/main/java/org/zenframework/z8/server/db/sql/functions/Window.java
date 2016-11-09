package org.zenframework.z8.server.db.sql.functions;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class Window extends SqlToken {
	private SqlToken token;
	private String name;

	protected Window(SqlToken token, String name) {
		this.token = token;
		this.name = name;
	}

	@Override
	public void collectFields(Collection<IValue> fields) {
		token.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) throws UnknownDatabaseException {
		String result = options.isAggregationEnabled() ? name + "(" : "";

		options.disableAggregation();
		result += token.format(vendor, options);
		options.enableAggregation();

		result += options.isAggregationEnabled() ? ")" : "";
		return result;
	}

	@Override
	public FieldType type() {
		return token.type();
	}
}
