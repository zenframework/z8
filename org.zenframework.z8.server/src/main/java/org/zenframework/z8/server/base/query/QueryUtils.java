package org.zenframework.z8.server.base.query;

import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.ILink;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class QueryUtils {
	static public Query findByClassId(Collection<Query> queries, String classId) {
		for(Query query : queries) {
			if(query.classId().equals(classId))
				return query;
		}
		return null;
	}

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

		if(type == FieldType.String || type == FieldType.Text || type == FieldType.Attachments)
			field.set(new string(value));
		else if(type == FieldType.Integer)
			field.set(value == null || value.isEmpty() ? integer.zero() : new integer(value));
		else if(type == FieldType.Decimal)
			field.set(value == null || value.isEmpty() ? decimal.zero() : new decimal(value));
		else if(type == FieldType.Boolean)
			field.set(value == null || value.isEmpty() ? bool.False : new bool(value));
		else if(type == FieldType.Date || type == FieldType.Datetime)
			field.set(value == null || value.isEmpty() ? date.Min : new date(value));
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

	static public void setFieldValues(Query query, String json) {
		if(json == null || json.isEmpty())
			return;

		JsonObject record = new JsonObject(json);

		for(String fieldId : JsonObject.getNames(record)) {
			Field field = query.findFieldById(fieldId);
			if(field != null)
				QueryUtils.setFieldValue(field, record.getString(fieldId));
		}
	}

	/*
	 *
	 * ["field", "field", ...] или [{ id: "field" }, { id: "field" }, ...]
	 *
	 */
	static public Collection<Field> parseFormFields(Query query, String json) {
		if(json == null || json.isEmpty())
			return query.fields();

		return parseFields(query, new JsonArray(json));
	}

	static public Collection<Field> parseFields(Query query, JsonArray json) {
		return parseFields(query, json, null);
	}

	static public Collection<Field> parseFields(Query query, JsonArray json, String context) {
		Collection<Field> fields = new ArrayList<Field>();

		if(json.length() == 0)
			return query.fields();

		for(int index = 0; index < json.length(); index++) {
			Object object = json.get(index);
			String name = (context != null && !context.isEmpty() ? context + '.' : "") +
					(object instanceof JsonObject ? ((JsonObject)object).getString(Json.id) : (String)object);
			Field field = query.findFieldById(name);
			if(field != null)
				fields.add(field);
		}

		return fields;
	}
}
