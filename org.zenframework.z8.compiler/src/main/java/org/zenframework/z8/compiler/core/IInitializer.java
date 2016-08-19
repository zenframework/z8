package org.zenframework.z8.compiler.core;

public interface IInitializer extends ILanguageElement {
	@Override
	IAttribute[] getAttributes();

	@Override
	IType getDeclaringType();

	@Override
	IVariableType getVariableType();

	String getLeftName();

	String getRightName();

	ILanguageElement getLeftElement();

	ILanguageElement getRightElement();

	IToken getOperator();
}
