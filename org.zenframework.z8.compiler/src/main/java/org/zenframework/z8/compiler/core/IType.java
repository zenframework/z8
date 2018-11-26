package org.zenframework.z8.compiler.core;

import org.zenframework.z8.compiler.parser.type.ImportBlock;
import org.zenframework.z8.compiler.parser.type.ImportElement;
import org.zenframework.z8.compiler.parser.type.members.MemberInit;
import org.zenframework.z8.compiler.parser.type.members.TypeBody;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public interface IType extends ILanguageElement {
	IAttribute findAttribute(String name);

	boolean getRecursionFlag();

	void setRecursionFlag(boolean recursionFlag);

	IToken getNameToken();

	String getUserName();

	String getJavaName();

	String getNestedUserName();

	String getNestedJavaName();

	String getUserPackage();

	String getJavaPackage();

	String getQualifiedJavaName();

	String getQualifiedUserName();

	boolean isQualified();
	void setQualified(boolean qualified);

	@Override
	IVariableType getVariableType();

	IType getBaseType();

	void setBaseType(IType baseType);

	boolean canBeBaseTypeOf(IType type);

	boolean canBeSubtypeOf(IType type);

	IType getContainerType();

	IType getTopLevelContainerType();

	IType[] getNestedTypes();

	IType getNestedType(String name);

	void addNestedType(IType type);

	IType lookupNestedType(String qualifiedName);

	boolean isEnum();

	boolean isNative();

	boolean isPrimary();

	boolean isAbstract();

	boolean extendsPrimary();

	boolean isFinal();

	IMember[] getMembers();

	IMember[] getAllMembers();

	IMethod[] getMethods();

	IMethod[] getTypeCastOperators();

	IInitializer[] getInitializers();

	IInitializer[] getAllInitializers();

	IMember getMember(String name);

	IMethod getMethod(String signature);

	IInitializer getInitializer(String name);

	IMember findMember(String name);

	IMethod findMethod(String signature);

	IInitializer findInitializer(String name);

	ITypeCast getCastTo(IVariableType variableType);

	void addMember(IMember member);

	void addMethod(IMethod method);

	void addTypeCastOperator(IMethod operator);

	void addInitializer(IInitializer initializer);

	boolean isSubtypeOf(IType candidate);

	boolean isContainerOf(IType candidate);

	boolean hasPrivateAccess(IType from);

	boolean hasProtectedAccess(IType from);

	String getConstructionStage();

	void setConstructionStage(String stage);

	IMember[] getAutoArrays();

	IMethod[] getMatchingMethods(String name);

	IMethod[] getMatchingMethods(String name, int parametrsCount);

	ImportBlock getImportBlock();

	void setImportBlock(ImportBlock importBlock);

	ImportElement[] getImports();

	TypeBody getTypeBody();

	MemberInit getMemberInit(String left, IType context);

	boolean resolveType(CompilationUnit compilationUnit);

	boolean checkImportUsage(CompilationUnit compilationUnit);

	public IInitializer[] findInitializers(String name);

	public IInitializer findInitializerDeep(String name);

	public boolean isSubtypeOf(String typeName);
}
