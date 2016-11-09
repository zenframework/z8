package org.zenframework.z8.server.base.model.actions;

import java.util.Collection;

import org.zenframework.z8.server.base.model.sql.Update;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.query.QueryUtils;
import org.zenframework.z8.server.base.table.value.Field;
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

		Query query = getQuery();
		Field primaryKey = query.primaryKey();

		for(int index = 0; index < records.length(); index++) {
			JsonObject record = (JsonObject)records.get(index);

			QueryUtils.parseRecord(record, query);

			guid recordId = primaryKey.guid();

			run(query, recordId);
		}
	}

	static public int run(Query query, guid recordId) {
		return run(query, recordId, true);
	}

	static public int run(Query query, guid recordId, boolean resetChangedFields) {
		int result = 0;

		if(recordId == null || !recordId.equals(guid.NULL)) {
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
