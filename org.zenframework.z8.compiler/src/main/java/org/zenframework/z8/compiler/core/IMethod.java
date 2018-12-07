package org.zenframework.z8.compiler.core;

public interface IMethod extends IMember {
	public boolean isVirtual();
	public boolean isNative();

	public void openLocalScope();
	public void closeLocalScope();

	public void addLocalVariable(IVariable variable);
	public String createTempVariable();

	public IVariable[] getLocalVariables();
	public IVariable findLocalVariable(String name);

	public int getParametersCount();

	public IVariable[] getParameters();
	public IVariableType[] getParameterTypes();
	public String[] getParameterNames();

	public IPosition getNamePosition();

	public ILanguageElement getBody();
}