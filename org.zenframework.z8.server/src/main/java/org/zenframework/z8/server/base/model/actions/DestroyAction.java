package org.zenframework.z8.server.base.model.actions;

import org.zenframework.z8.server.base.model.sql.Delete;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.guid;

public class DestroyAction extends Action {
	public DestroyAction(ActionParameters parameters) {
		super(parameters);
	}

	@Override
	public void writeResponse(JsonWriter writer) {
		String jsonData = getDataParameter();

		if(jsonData.charAt(0) != '[') {
			jsonData = "[" + jsonData + "]";
		}

		JsonArray records = new JsonArray(jsonData);

		boolean transactive = getQuery().isTransactive() || records.length() > 1;
		Connection connection = transactive ? ConnectionManager.get() : null;

		try {
			if(transactive)
				connection.beginTransaction();

			destroy(records);

			if(transactive)
				connection.commit();
		} catch(Throwable e) {
			if(transactive)
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
