package org.zenframework.z8.server.base.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GeometryExpression;
import org.zenframework.z8.server.base.table.value.GeometryField;
import org.zenframework.z8.server.base.table.value.ILink;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.geometry.parser.GeoJsonReader;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.geometry;
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

	static public Query findByIndex(Collection<Query> queries, String index) {
		for(Query query : queries) {
			if(query.index().equals(index))
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

	static public void setFieldValue(Field field, String value, Collection<file> files) {
		FieldType type = field.type();

		if(type == FieldType.String || type == FieldType.Text || type == FieldType.Attachments || type == FieldType.File)
			field.set(new string(value));
		else if(type == FieldType.Geometry)
			field.set(value == null || value.isEmpty() ? new geometry(srs(field)) : GeoJsonReader.read(value, srs(field)));
		else if(type == FieldType.Integer)
			field.set(value == null || value.isEmpty() ? field.getDefaultValue() : new integer(value));
		else if(type == FieldType.Decimal)
			field.set(value == null || value.isEmpty() ? field.getDefaultValue() : new decimal(value));
		else if(type == FieldType.Boolean)
			field.set(value == null || value.isEmpty() ? field.getDefaultValue() : new bool(value));
		else if(type == FieldType.Date || type == FieldType.Datetime)
			field.set(value == null || value.isEmpty() ? field.getDefaultValue() : new date(value));
		else if(type == FieldType.Datespan)
			field.set(value == null || value.isEmpty() ? field.getDefaultValue() : new datespan(value));
		else if(type == FieldType.Guid)
			field.set(value == null || value.isEmpty() ? field.getDefaultValue() : new guid(value));
		else if(type == FieldType.Binary)
			field.set(new binary(((List<file>)files).get(Integer.parseInt(value)).getInputStream()));
		else
			throw new UnsupportedOperationException();
	}

	static public guid extractKey(JsonObject record, Field field) {
		if(field == null)
			return null;

		String key = field.id();
		return record.has(key) ? record.getGuid(key) : null;
	}

	static private JsonObject parse(String json) {
		if(json == null || json.isEmpty())
			return null;

		if(!json.startsWith("["))
			return new JsonObject(json);

		JsonArray array = new JsonArray(json);
		return array.isEmpty() ? null : array.getJsonObject(0);
	}

	static public void setFieldValues(Query query, String json) {
		JsonObject record = parse(json);

		if(record == null)
			return;

		for(String fieldId : JsonObject.getNames(record)) {
			Field field = query.findFieldById(fieldId);
			if(field != null && field.type() != FieldType.Binary)
				QueryUtils.setFieldValue(field, record.getString(fieldId), null);
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
			// TODO Why field.type() != FieldType.Binary ??? Remove later
			if(field != null /*&& field.type() != FieldType.Binary*/)
				fields.add(field);
		}

		return fields;
	}
	
	static private int srs(Field field) {
		if (field instanceof GeometryField)
			return ((GeometryField) field).srs.getInt();
		if (field instanceof GeometryExpression)
			return ((GeometryExpression) field).srs.getInt();
		throw new UnsupportedOperationException();
	}

}
