package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.primary;

public interface IValue extends IObject {
	public FieldType type();

	public primary get();

	public void set(primary b);

	public boolean wasNull();
	public boolean changed();
}
