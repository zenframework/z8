package org.zenframework.z8.server.request.actions;

import java.util.Collection;

import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.form.action.Parameter;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.guid;

public class CommandAction extends RequestAction {
	public CommandAction(ActionConfig config) {
		super(config);
	}

	@Override
	public void writeResponse(JsonWriter writer) {
		Query context = getContextQuery();
		Query query = getQuery();

		String name = getRequestParameter(Json.name);
		Action action = context.findActionById(name);

		if(action == null)
			throw new RuntimeException("Action '" + name + "' is not found in " + context.displayName());

		parseParameters(action);

		Connection connection = action.useTransaction.get() ? ConnectionManager.get() : null;

		try {
			if(connection != null)
				connection.beginTransaction();

			Collection<guid> records = getGuidCollection(Json.records);
			Collection<guid> selected = getGuidCollection(Json.selection);

			action.execute(records, context, selected, query);

			if(connection != null)
				connection.commit();
		} catch(Throwable e) {
			if(connection != null)
				connection.rollback();

			throw new RuntimeException(e);
		}
	}

	private void parseParameters(Action action) {
		JsonArray parameters = new JsonArray(getRequestParameter(Json.parameters));

		for(int i = 0; i < parameters.length(); i++) {
			JsonObject object = parameters.getJsonObject(i);
			Parameter parameter = action.getParameter(object.getString(Json.id));
			parameter.parse(object.has(Json.value) ? object.getString(Json.value) : null);
		}
	}
}
