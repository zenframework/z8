package org.zenframework.z8.compiler.core;

import org.zenframework.z8.compiler.workspace.CompilationUnit;

public interface ILanguageElement extends IAttributed, ISource {
	public ILanguageElement getParent();
	public void setParent(ILanguageElement parent);

	public IType getDeclaringType();
	public IMethod getDeclaringMethod();

	public boolean getStaticContext();
	public void setStaticContext(boolean context);

	public IVariable getVariable();
	public IVariableType getVariableType();

	public boolean hasError();
	public boolean hasFatalError();
	public void setError(IPosition position, String message);
	public void setFatalError(IPosition position, String message);

	public boolean resolveTypes(CompilationUnit source, IType declaringType);
	public boolean resolveStructure(CompilationUnit source, IType declaringType);
	public boolean checkSemantics(CompilationUnit source, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context);
	public boolean resolveNestedTypes(CompilationUnit source, IType declaringType);

	public boolean isQualifiedName();
	public boolean isOperatorNew();

	public void getCode(CodeGenerator codeGenerator);
	public void getClassCode(CodeGenerator codeGenerator);
	public void getStaticBlock(CodeGenerator codeGenerator);
	public void getStaticConstructor(CodeGenerator codeGenerator);
	public void getConstructor(CodeGenerator codeGenerator);
	public void getConstructor1(CodeGenerator codeGenerator);
	public void getConstructor2(CodeGenerator codeGenerator);

	public ILanguageElement getElementAt(int offset);
	public ILanguageElement getElementAt(int offset, int length);
	public ILanguageElement getElementAt(IPosition position);

	public boolean containsQualifiedName(String name);
}
