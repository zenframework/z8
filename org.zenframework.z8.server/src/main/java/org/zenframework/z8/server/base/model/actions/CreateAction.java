package org.zenframework.z8.server.base.model.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.query.QueryUtils;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
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
		String jsonData = getDataParameter();

		if(jsonData.charAt(0) == '{')
			jsonData = "[" + jsonData + "]";

		JsonArray records = new JsonArray(jsonData);

		boolean transactive = records.length() > 1;
		Connection connection = ConnectionManager.get();

		try {
			if(transactive)
				connection.beginTransaction();

			Collection<guid> recordIds = insert(records);

			if(transactive)
				connection.commit();

			writeFormFields(writer, getQuery(), recordIds);
		} catch(Throwable e) {
			if(transactive)
				connection.rollback();
			throw new RuntimeException(e);
		}
	}

	private Collection<guid> insert(JsonArray records) {
		Collection<guid> result = new ArrayList<guid>();

		Query query = getQuery();
		Field primaryKey = query.primaryKey();
		Field parentKey = query.parentKey();

		Map<String, Field> fieldsMap = new HashMap<String, Field>();

		for(int index = 0; index < records.length(); index++) {
			JsonObject record = (JsonObject)records.get(index);

			guid recordId = QueryUtils.extractKey(record, primaryKey);
			guid parentId = QueryUtils.extractKey(record, parentKey);

			if(recordId == null || recordId.isNull())
				recordId = guid.create();

			NewAction.run(query, recordId, parentId);

			for(String fieldId : JsonObject.getNames(record)) {
				Field field = fieldsMap.get(fieldId);

				if(field == null) {
					field = query.getFieldById(fieldId);
					if(field == null)
						continue;
					fieldsMap.put(fieldId, field);
				}

				String value = record.getString(fieldId);
				QueryUtils.setFieldValue(field, value);
			}

			primaryKey.set(recordId);

			query.insert(recordId, parentId);

			result.add(recordId);
		}

		return result;
	}
}
