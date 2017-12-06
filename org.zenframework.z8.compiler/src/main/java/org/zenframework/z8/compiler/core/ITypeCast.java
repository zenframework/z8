package org.zenframework.z8.compiler.core;

public interface ITypeCast {
	public IVariableType getSource();

	public IVariableType getTarget();

	public int compare(ITypeCast other);

	public boolean isBaseTypeCast();
	public int getWeight();

	public boolean hasOperator();

	public IMethod getContext();

	public void setContext(IMethod method);

	public IMethod getOperator();

	public void setOperator(IMethod operator);

	public int distanceToBaseType();

	public void getCode(CodeGenerator codeGenerator, ILanguageElement element);
}
