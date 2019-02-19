package org.zenframework.z8.server.db.sql.functions.string;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class GetJson extends StringFunction {
	private final SqlToken json;
	private final SqlToken element;

	public GetJson(SqlToken json, SqlToken element) {
		this.json = json;
		this.element = element;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		json.collectFields(fields);
		element.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		StringBuilder result = new StringBuilder();

		if(vendor == DatabaseVendor.Postgres)
			result.append("(").append(json.format(vendor, options)).append(")::json->>").append(element.format(vendor, options));
		else
			throw new UnknownDatabaseException();

		return result.toString();
	}
}
