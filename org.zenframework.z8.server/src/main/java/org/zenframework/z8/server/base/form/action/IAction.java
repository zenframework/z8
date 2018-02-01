package org.zenframework.z8.server.base.form.action;

import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.request.INamedObject;

public interface IAction extends INamedObject {
	public void write(JsonWriter writer, String requestId);

	public IParameter getParameter(String id);
}
