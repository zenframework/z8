package org.zenframework.z8.compiler.core;

public interface IInitializer extends ILanguageElement {
	@Override
	public IAttribute[] getAttributes();

	@Override
	public IType getDeclaringType();

	@Override
	public IVariableType getVariableType();

	public String getName();

	public ILanguageElement getLeftElement();
	public ILanguageElement getRightElement();

	public IToken getOperator();
}
