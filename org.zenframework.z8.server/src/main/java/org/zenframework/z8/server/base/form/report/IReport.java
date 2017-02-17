package org.zenframework.z8.server.base.form.report;

import java.util.Collection;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.request.INamedObject;

public interface IReport extends INamedObject {
	public void write(JsonWriter writer);

	public Collection<Query> queries();
}
