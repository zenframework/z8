package org.zenframework.z8.server.base.model.actions;

import org.zenframework.z8.server.base.model.command.IParameter;
import org.zenframework.z8.server.base.view.command.Command;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.json.parser.JsonObject;

public class CommandAction extends Action {
	public CommandAction(ActionParameters parameters) {
		super(parameters);
	}

	@Override
	public void writeResponse(JsonObject writer) {
		Command command = getQuery().getCommand(getCommandParameter());

		JsonObject object = new JsonObject(getParametersParameter());

		if(object != null) {
			String[] names = JsonObject.getNames(object);

			if(names != null) {
				for(String parameterId : names) {
					IParameter parameter = command.getParameter(parameterId);
					String value = object.getString(parameterId);
					parameter.parse(value);
				}
			}
		}

		Connection connection = command.useTransaction.get() ? ConnectionManager.get() : null;

		try {
			if(connection != null)
				connection.beginTransaction();

			getQuery().onCommand(command, getIdList());

			if(connection != null)
				connection.commit();
		} catch(Throwable e) {
			if(connection != null)
				connection.rollback();

			throw new RuntimeException(e);
		}
	}
}
