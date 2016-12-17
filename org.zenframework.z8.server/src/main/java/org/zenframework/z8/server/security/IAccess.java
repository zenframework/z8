package org.zenframework.z8.server.security;

import java.io.Serializable;

import org.zenframework.z8.server.engine.RmiSerializable;

public interface IAccess extends RmiSerializable, Serializable {
	public boolean read();
	public void setRead(boolean read);

	public boolean write();
	public void setWrite(boolean write);

	public boolean create();
	public void setCreate(boolean create);

	public boolean copy();
	public void setCopy(boolean copy);

	public boolean destroy();
	public void setDestroy(boolean destroy);

	public boolean execute();
	public void setExecute(boolean execute);

	public void apply(IAccess access);
}
