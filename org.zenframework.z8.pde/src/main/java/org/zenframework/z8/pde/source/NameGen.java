package org.zenframework.z8.pde.source;

public abstract class NameGen {

	public abstract void nameGenRemove();

	public abstract boolean nameGenCanRemove(String pre);

	public abstract void nameGenAdd(String pre);

	public abstract String nameGenResult();

}
