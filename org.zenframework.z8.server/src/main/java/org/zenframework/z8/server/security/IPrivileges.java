package org.zenframework.z8.server.security;

import java.io.Serializable;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.types.guid;

public interface IPrivileges extends RmiSerializable, Serializable {
	public void setDefaultAccess(IAccess access);

	public IAccess getAccess(Query table);
	public void setTableAccess(guid table, IAccess access);

	public IAccess getAccess(Field field);
	public void setFieldAccess(guid field, IAccess access);
}
