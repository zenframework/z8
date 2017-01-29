package org.zenframework.z8.compiler.core;

import org.zenframework.z8.compiler.workspace.CompilationUnit;

public interface ILanguageElement extends IAttributed, ISource {
	ILanguageElement getParent();

	void setParent(ILanguageElement parent);

	IType getDeclaringType();

	IMethod getDeclaringMethod();

	boolean getStaticContext();

	void setStaticContext(boolean context);

	IVariable getVariable();

	IVariableType getVariableType();

	boolean hasError();

	boolean hasFatalError();

	void setError(IPosition position, String message);

	void setFatalError(IPosition position, String message);

	boolean resolveTypes(CompilationUnit source, IType declaringType);

	boolean resolveStructure(CompilationUnit source, IType declaringType);

	boolean checkSemantics(CompilationUnit source, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context);

	boolean resolveNestedTypes(CompilationUnit source, IType declaringType);

	boolean isQualifiedName();

	boolean isOperatorNew();

	void getCode(CodeGenerator codeGenerator);

	void getClassCode(CodeGenerator codeGenerator);

	void getStaticBlock(CodeGenerator codeGenerator);

	void getStaticConstructor(CodeGenerator codeGenerator);

	void getConstructor(CodeGenerator codeGenerator);

	void getConstructor1(CodeGenerator codeGenerator);

	void getConstructor2(CodeGenerator codeGenerator);

	ILanguageElement getElementAt(int offset);

	ILanguageElement getElementAt(int offset, int length);

	ILanguageElement getElementAt(IPosition position);
}
