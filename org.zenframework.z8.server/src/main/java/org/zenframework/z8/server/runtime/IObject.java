package org.zenframework.z8.server.runtime;

import java.util.Collection;

public interface IObject extends IAttributed {
	public int controlSum();

	public IObject getContainer();
	public IObject getOwner();
	public void setOwner(IObject owner);

	public void initMembers();
	public Collection<IClass<? extends IObject>> members();
	public IClass<? extends IObject> getMember(String name);

	public IClass<? extends IObject> getCLASS();
	public void setCLASS(IClass<? extends IObject> cls);

	public void constructor1();
	public void constructor2();
	public void constructor();

	public String toDebugString();
}
