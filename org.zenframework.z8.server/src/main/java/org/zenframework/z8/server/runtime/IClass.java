package org.zenframework.z8.server.runtime;

public interface IClass<TYPE extends IObject> extends IAttributed {
	public final static int Constructor = -1;
	public final static int Constructor1 = 0;
	public final static int Constructor2 = 1;

	public Class<TYPE> getJavaClass();
	public void setJavaClass(Class<?> cls);

	public IObject getContainer();

	public IObject getOwner();
	public void setOwner(IObject owner);

	public int stage();

	public boolean hasInstance();
	public boolean instanceOf(Class<?> cls);

	public TYPE newInstance();

	public TYPE get();
	public TYPE get(int stage);

	public Object[] getClosure();
	public void setClosure(Object[] closure);
}
