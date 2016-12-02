package org.zenframework.z8.server.base.query;

import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.ILink;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class QueryUtils {
	static public Collection<Query> getOwners(Collection<ILink> links) {
		Collection<Query> queries = new ArrayList<Query>();

		for(ILink link : links) {
			Field field = (Field)link;
			queries.add(field.owner());
		}

		return queries;
	}

	static public void setFieldValue(Field field, String value) {
		FieldType type = field.type();

		if(type == FieldType.String || type == FieldType.Text)
			field.set(new string(value));
		else if(type == FieldType.Integer)
			field.set(value == null || value.isEmpty() ? new integer() : new integer(value));
		else if(type == FieldType.Decimal)
			field.set(value == null || value.isEmpty() ? new decimal() : new decimal(value));
		else if(type == FieldType.Boolean)
			field.set(value == null || value.isEmpty() ? new bool() : new bool(value));
		else if(type == FieldType.Datetime || type == FieldType.Date)
			field.set(value == null || value.isEmpty() ? new date(date.MIN) : new date(value));
		else if(type == FieldType.Datespan)
			field.set(value == null || value.isEmpty() ? new datespan() : new datespan(value));
		else if(type == FieldType.Guid)
			field.set(value == null || value.isEmpty() ? new guid() : new guid(value));
		else
			throw new UnsupportedOperationException();
	}

	static public guid extractKey(JsonObject record, Field field) {
		if(field == null)
			return null;

		String key = field.id();
		return record.has(key) ? record.getGuid(key) : null;
	}
}
