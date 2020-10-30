package org.zenframework.z8.server.security;

import java.io.Serializable;

import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.types.guid;

public interface IRole extends RmiSerializable, Serializable {
	public guid id();
	public String name();
	public IAccess access();
}
