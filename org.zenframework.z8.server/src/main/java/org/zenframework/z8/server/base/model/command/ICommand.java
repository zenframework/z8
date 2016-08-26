package org.zenframework.z8.server.base.model.command;

import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.request.INamedObject;

public interface ICommand extends INamedObject {
	public void write(JsonWriter writer);

	public IParameter getParameter(String id);
}
