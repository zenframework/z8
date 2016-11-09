package org.zenframework.z8.server.base.model.actions;

import java.util.Collection;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.query.QueryUtils;
import org.zenframework.z8.server.base.query.Style;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.guid;

public class CreateAction extends Action {
	public CreateAction(ActionParameters parameters) {
		super(parameters);
	}

	@Override
	public void writeResponse(JsonWriter writer) {
		boolean newAndSave = actionParameters().getBoolean(Json.save);

		String jsonData = getDataParameter();

		if(jsonData.charAt(0) == '{')
			jsonData = "[" + jsonData + "]";

		JsonArray records = new JsonArray(jsonData);

		Query query = getQuery();
		Query rootQuery = getRootQuery();

		Field primaryKey = rootQuery.primaryKey();
		Field parentKey = rootQuery.parentKey();

		writer.startArray(Json.data);

		for(int index = 0; index < records.length(); index++) {
			JsonObject record = (JsonObject)records.get(index);

			guid recordId = extractKey(record, primaryKey);
			guid parentId = extractKey(record, parentKey);

			if(recordId == null || recordId.isNull())
				recordId = guid.create();

			if(newAndSave)
				NewAction.run(query, recordId, parentId);

			QueryUtils.parseRecord(record, query);

			primaryKey.set(recordId);

			query.insert(recordId, parentId);

			Collection<Field> fields = query.getFormFields();
			fields.add(primaryKey);

			if(query.readRecord(recordId, fields)) {
				writer.startObject();

				for(Field field : fields)
					field.writeData(writer);

				Style style = query.renderRecord();

				if(style != null)
					style.write(writer);

				writer.finishObject();
			}
		}

		writer.finishArray();
	}

	private guid extractKey(JsonObject record, Field field) {
		if(field == null)
			return null;

		String key = field.id();
		return record.has(key) ? record.getGuid(key) : null;
	}
}
