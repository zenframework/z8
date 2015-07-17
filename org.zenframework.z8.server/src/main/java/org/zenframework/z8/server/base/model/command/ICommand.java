package org.zenframework.z8.server.base.model.command;

import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.request.INamedObject;

public interface ICommand extends INamedObject {
    public void write(JsonObject writer);

    public IParameter getParameter(String id);
}
