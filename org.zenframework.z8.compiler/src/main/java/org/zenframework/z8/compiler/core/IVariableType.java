package org.zenframework.z8.compiler.core;

public interface IVariableType extends ILanguageElement {
	IType getType();

	String getSignature();

	String getJavaName();

	String getDeclaringJavaName();

	String getJavaNew();

	String getJavaNew(boolean staticContext);

	boolean isQualified();

	boolean isNull();

	boolean isAuto();

	boolean isEnum();

	boolean isArray();

	boolean isStatic();

	boolean isReference();

	boolean isArrayOfReferences();

	boolean extendsPrimary();

	int getDimensions();

	IType[] getKeys();

	IType getRightKey();

	IType getLeftKey();

	void addLeftKey(IType type);

	void addRightKey(IType type);

	IType removeLeftKey();

	IType removeRightKey();

	public ITypeCast getCastTo(IType candidate);

	public ITypeCast getCastTo(IVariableType candidate);

	public ITypeCast getCastTo(IType context, IVariableType candidate);

	boolean compare(IVariableType variableType);

	IMember findMember(String name);

	IMethod[] getMatchingMethods(String name);

	IMethod[] getTypeCastOperators();

	IMember[] getAllMembers();
}
