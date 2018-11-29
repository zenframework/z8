package org.zenframework.z8.compiler.core;

public interface IVariable extends IAttributed, ISource {
	public IVariableType getVariableType();

	public String getName();

	public String getJavaName();

	public String getUserName();

	public String getSignature();

	public boolean isFinal();

	public int getClosure();
	public void setClosure(int closure);
}
