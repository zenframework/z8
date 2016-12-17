package org.zenframework.z8.server.base.model.actions;

import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.model.sql.Update;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.query.QueryUtils;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.guid;

public class UpdateAction extends Action {
	public UpdateAction(ActionParameters parameters) {
		super(parameters);
	}

	@Override
	public void writeResponse(JsonWriter writer) {
		String jsonData = getDataParameter();

		if(jsonData.charAt(0) == '{')
			jsonData = "[" + jsonData + "]";

		JsonArray records = new JsonArray(jsonData);

		boolean transactive = getQuery().isTransactive() || records.length() > 1;
		Connection connection = transactive ? ConnectionManager.get() : null;

		try {
			if(transactive)
				connection.beginTransaction();

			Collection<guid> recordIds = update(records);

			if(transactive)
				connection.commit();

			writeFormFields(writer, getRequestQuery(), recordIds);
		} catch(Throwable e) {
			if(transactive)
				connection.rollback();
			throw new RuntimeException(e);
		}
	}

	private Collection<guid> update(JsonArray records) {
		Query query = getQuery();

		Collection<guid> result = new ArrayList<guid>();

		for(int index = 0; index < records.length(); index++) {
			JsonObject record = (JsonObject)records.get(index);

			for(String fieldId : JsonObject.getNames(record)) {
				Field field = query.findFieldById(fieldId);
				if(field != null)
					QueryUtils.setFieldValue(field, record.getString(fieldId));
			}

			guid recordId = query.primaryKey().guid();
			guid rootRecordId = QueryUtils.extractKey(record, getRequestQuery().primaryKey());

	
			if(recordId.isNull())
				createLink(query, rootRecordId);
			else
				run(query, recordId);

			result.add(rootRecordId);
		}

		return result;
	}

	private void createLink(Query query, guid rootRecordId) {
		Connection connection = ConnectionManager.get();
		connection.beginTransaction();

		try {
			Field primaryKey = query.primaryKey();
			Field parentKey = query.parentKey();

			guid newRecordId = guid.create();
			primaryKey.set(newRecordId);

			guid parentId = parentKey != null ? parentKey.guid() : null;

			NewAction.run(query, newRecordId, parentId);
			query.insert(newRecordId, parentId);

			Query requestQuery = getRequestQuery();
			Link link = getLink();

			if(query == requestQuery)
				throw new RuntimeException("UpdateAction - bad recordId"); 

			link.set(newRecordId);
			requestQuery.update(rootRecordId);

			connection.commit();
		} catch(Throwable e) {
			connection.rollback();
			throw new RuntimeException(e);
		}
	}

	static public int run(Query query, guid recordId) {
		return run(query, recordId, true);
	}

	static public int run(Query query, guid recordId, boolean resetChangedFields) {
		int result = 0;

		if(recordId == null || !recordId.equals(guid.Null)) {
			if(recordId != null)
				query.beforeUpdate(recordId);

			Collection<Field> changedFields = query.getChangedFields();

			result = changedFields.isEmpty() ? 0 : new Update(query, changedFields, recordId).execute();

			if(recordId != null)
				query.afterUpdate(recordId);

			if(resetChangedFields) {
				for(Field field : changedFields)
					field.reset();
			}
		}

		return result;
	}
}
