package org.zenframework.z8.server.request.actions;

import java.util.Collection;

import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.form.action.IParameter;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
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

		String actionId = getRequestParameter(Json.id);
		Action action = context.findActionById(actionId);

		if(action == null)
			throw new RuntimeException("Action '" + actionId + "' is not found in " + context.displayName());

		JsonObject object = new JsonObject(getParametersParameter());

		if(object != null) {
			String[] names = JsonObject.getNames(object);

			if(names != null) {
				for(String parameterId : names) {
					IParameter parameter = action.getParameter(parameterId);
					String value = object.getString(parameterId);
					parameter.parse(value);
				}
			}
		}

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
}
