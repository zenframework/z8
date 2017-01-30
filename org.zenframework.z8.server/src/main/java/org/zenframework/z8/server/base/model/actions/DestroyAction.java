package org.zenframework.z8.server.base.model.actions;

import org.zenframework.z8.server.base.model.sql.Delete;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.exceptions.AccessRightsViolationException;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.guid;

public class DestroyAction extends RequestAction {
	public DestroyAction(ActionConfig config) {
		super(config);
	}

	@Override
	public void writeResponse(JsonWriter writer) {
		if(!getQuery().access().destroy())
			throw new AccessRightsViolationException();

		String jsonData = getDataParameter();

		if(jsonData.charAt(0) != '[')
			jsonData = "[" + jsonData + "]";

		JsonArray records = new JsonArray(jsonData);

		Connection connection = ConnectionManager.get();

		try {
			connection.beginTransaction();
			destroy(records);
			connection.commit();
		} catch(Throwable e) {
			connection.rollback();
			throw new RuntimeException(e);
		}
	}

	private void destroy(JsonArray records) {
		for(int index = 0; index < records.length(); index++) {
			Object object = records.get(index);
			String property = getQuery().primaryKey().id();
			// data: [{recordId: guid}] or data: [guid]
			String value = object instanceof JsonObject ? ((JsonObject)object).getString(property) : (String)object;
			guid recordId = new guid(value);
			run(getQuery(), recordId);
		}
	}

	static public int run(Query query, guid id) {
		int result = 0;

		if(!guid.Null.equals(id)) {
			query.beforeDestroy(id);
			result = new Delete(query, id).execute();
			query.afterDestroy(id);
		}

		return result;
	}
}
