package org.zenframework.z8.server.base.form.action;

import org.zenframework.z8.server.json.JsonWriter;

public interface IAction {
	public void write(JsonWriter writer, String requestId);

	public IParameter getParameter(String id);
}
