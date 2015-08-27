package org.zenframework.z8.server.base.model.actions;

import org.zenframework.z8.server.base.model.command.IParameter;
import org.zenframework.z8.server.base.view.command.Command;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.request.IResponse;

public class CommandAction extends Action {
    public CommandAction(ActionParameters parameters) {
        super(parameters);
    }

    @Override
    public void processRequest(IResponse response) throws Throwable {
        Connection connection = ConnectionManager.get();

        try {
            connection.beginTransaction();
            super.processRequest(response);
            connection.commit();
        }
        catch(Throwable e) {
            connection.rollback();
            throw e;
        }
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

        getQuery().onCommand(command, getIdList());
    }
}
