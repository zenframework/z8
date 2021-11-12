package org.zenframework.z8.server.request.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.query.QueryUtils;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.exceptions.AccessRightsViolationException;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;

public class CreateAction extends RequestAction {
	public CreateAction(ActionConfig config) {
		super(config);
	}

	@Override
	public void writeResponse(JsonWriter writer) {
		if(!getQuery().access().getCreate())
			throw new AccessRightsViolationException();

		String jsonData = getRequestParameter(Json.data);

		if(jsonData == null || jsonData.isEmpty())
			throw new RuntimeException("CreateAction - bad data parameter"); 

		JsonArray records = new JsonArray(jsonData);

		Connection connection = ConnectionManager.get();

		try {
			connection.beginTransaction();
			Collection<guid> recordIds = insert(records);
			connection.commit();

			writeFormFields(writer, getQuery(), recordIds);
		} catch(Throwable e) {
			connection.rollback();
			throw new RuntimeException(e);
		}
	}

	private Collection<guid> insert(JsonArray records) {
		Collection<guid> result = new ArrayList<guid>();

		Query query = getQuery();
		Field primaryKey = query.primaryKey();

		Map<String, Field> fieldsMap = new HashMap<String, Field>();

		Collection<file> files = request().getFiles();

		for(int index = 0; index < records.length(); index++) {
			JsonObject record = (JsonObject)records.get(index);

			guid recordId = QueryUtils.extractKey(record, primaryKey);

			if(recordId == null || recordId.isNull())
				recordId = guid.create();

			query.onNew();

			for(String fieldId : record.getNames()) {
				Field field = fieldsMap.get(fieldId);

				if(field == null) {
					field = query.findFieldById(fieldId);
					if(field == null)
						continue;
					fieldsMap.put(fieldId, field);
				}

				QueryUtils.setFieldValue(field, record.getString(fieldId), files);
			}

			primaryKey.set(recordId);

			query.onCreateAction(recordId);
			query.insert(recordId);

			result.add(recordId);
		}

		return result;
	}
}
