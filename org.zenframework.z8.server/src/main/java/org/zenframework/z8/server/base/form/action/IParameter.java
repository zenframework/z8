package org.zenframework.z8.server.base.form.action;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.request.INamedObject;

public interface IParameter extends INamedObject {
	public FieldType getType();

	public Object get();

	public void set(Object value);

	public void parse(String value);

	public void write(JsonWriter writer);
}
