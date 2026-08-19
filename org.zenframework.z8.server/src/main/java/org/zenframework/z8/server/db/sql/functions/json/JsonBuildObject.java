package org.zenframework.z8.server.db.sql.functions.json;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class JsonBuildObject extends SqlToken {
	private Map<String, SqlToken> values = new HashMap<String, SqlToken>();

	public JsonBuildObject(Map<String, SqlToken> values) {
		if(values != null)
			this.values.putAll(values);
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		for(SqlToken value : values.values())
			value.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		if(values == null || values.isEmpty()) {
			switch(vendor) {
			case Postgres:
				return "'{}'::json";
			case Oracle:
			case H2:
				return "JSON_OBJECT()";
			case SqlServer:
				return "'{}'";
			default:
				throw new UnknownDatabaseException();
			}
		}

		switch(vendor) {
		case Postgres: {
			StringJoiner result = new StringJoiner(", ", "json_strip_nulls(json_build_object(", "))");
			values.forEach((key, value) -> result.add("'" + key + "', " + value.format(vendor, options)));
			return result.toString();
		}
		case Oracle:
		case H2: {
			StringJoiner result = new StringJoiner(", ", "JSON_OBJECT(", ")");
			values.forEach((key, value) -> result.add("KEY '" + key + "' VALUE " + value.format(vendor, options)));
			return result.toString();
		}
		case SqlServer: {
			StringJoiner result = new StringJoiner(", ", "( SELECT ", " FOR JSON PATH, WITHOUT_ARRAY_WRAPPER )");
			values.forEach((key, value) -> result.add(value.format(vendor, options) + " AS [" + key + "]"));
			return result.toString();
		}
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.String;
	}
}
