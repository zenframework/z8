package org.zenframework.z8.server.request.actions;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.Delete;
import org.zenframework.z8.server.exceptions.AccessRightsViolationException;
import org.zenframework.z8.server.json.Json;
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
		if(!getQuery().access().getDestroy())
			throw new AccessRightsViolationException();

		JsonArray records = new JsonArray(getRequestParameter(Json.data));

		if(records.isEmpty())
			throw new RuntimeException("DestroyAction - bad data parameter"); 

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
		Query query = getQuery();

		for(int index = 0; index < records.length(); index++) {
			Object object = records.get(index);
			String property = getQuery().primaryKey().id();
			// data: [{recordId: guid}] or data: [guid]
			String value = object instanceof JsonObject ? ((JsonObject)object).getString(property) : (String)object;
			guid recordId = new guid(value);

			query.onDestroyAction(recordId);
			run(query, recordId);
		}
	}

	static public int run(Query query, guid id) {
		int result = 0;

		if(!guid.Null.equals(id)) {
			query.beforeDestroy(id);
			result = Delete.create(query, id).execute();
			query.afterDestroy(id);
		}

		return result;
	}
}
