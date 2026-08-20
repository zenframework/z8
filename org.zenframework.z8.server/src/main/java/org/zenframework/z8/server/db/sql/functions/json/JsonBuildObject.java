package org.zenframework.z8.server.db.sql.functions.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.string;

public class JsonBuildObject extends SqlToken {
	private static interface Property {
		string getName();
		SqlToken getValue();
	}

	private Collection<Property> properties = new ArrayList<Property>();

	public JsonBuildObject(Map<string, SqlToken> values) {
		for(Map.Entry<string, SqlToken> entry : values.entrySet())
			properties.add(new Property() {

				@Override
				public SqlToken getValue() {
					return entry.getValue();
				}

				@Override
				public string getName() {
					return entry.getKey();
				}
			});
	}

	public JsonBuildObject(Collection<Field> fields) {
		for(Field field : fields)
			properties.add(new Property() {

				@Override
				public SqlToken getValue() {
					return new SqlField(field);
				}

				@Override
				public string getName() {
					return new string(field.index());
				}
			});
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		for(Property property : properties)
			property.getValue().collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		if(properties.isEmpty()) {
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
			properties.forEach((property) -> result.add("'" + property.getName() + "', " + property.getValue().format(vendor, options)));
			return result.toString();
		}
		case Oracle:
		case H2: {
			StringJoiner result = new StringJoiner(", ", "JSON_OBJECT(", ")");
			properties.forEach((property) -> result.add("KEY '" + property.getName() + "' VALUE " + property.getValue().format(vendor, options)));
			return result.toString();
		}
		case SqlServer: {
			StringJoiner result = new StringJoiner(", ", "( SELECT ", " FOR JSON PATH, WITHOUT_ARRAY_WRAPPER )");
			properties.forEach((property) -> result.add(property.getValue().format(vendor, options) + " AS [" + property.getName() + "]"));
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
