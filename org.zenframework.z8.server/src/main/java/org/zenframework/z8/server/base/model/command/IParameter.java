package org.zenframework.z8.server.base.model.command;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.request.INamedObject;
import org.zenframework.z8.server.types.primary;

public interface IParameter extends INamedObject {
    public FieldType getType();

    public primary get();

    public void set(primary value);

    public void parse(String value);

    public void write(JsonObject writer);
}
