package org.zenframework.z8.server.base.form.action;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.json.JsonWriter;

public interface IAction {
	public void writeMeta(JsonWriter writer, Query query, Query context);

	public IParameter getParameter(String id);
}
