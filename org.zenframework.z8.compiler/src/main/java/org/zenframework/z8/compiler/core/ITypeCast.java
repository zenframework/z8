package org.zenframework.z8.compiler.core;

public interface ITypeCast {
	IVariableType getSource();

	IVariableType getTarget();

	int compare(ITypeCast other);

	int getWeight();

	boolean hasOperator();
	boolean isBaseTypeCast();

	IMethod getContext();

	void setContext(IMethod method);

	IMethod getOperator();

	void setOperator(IMethod operator);

	int distanceToBaseType();

	void getCode(CodeGenerator codeGenerator, ILanguageElement element);
}
