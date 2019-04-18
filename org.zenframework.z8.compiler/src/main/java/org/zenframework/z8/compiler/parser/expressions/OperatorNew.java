package org.zenframework.z8.compiler.parser.expressions;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class OperatorNew extends LanguageElement {
	private IToken newToken;
	private VariableType variableType;

	public OperatorNew(IToken newToken, QualifiedName typeName) {
		this.newToken = newToken;
		this.variableType = new VariableType(typeName);
	}

	@Override
	public IPosition getSourceRange() {
		return newToken.getPosition().union(variableType.getPosition());
	}

	@Override
	public IToken getFirstToken() {
		return newToken;
	}

	@Override
	public IVariableType getVariableType() {
		return variableType;
	}

	@Override
	public boolean isOperatorNew() {
		return true;
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveTypes(compilationUnit, declaringType))
			return false;

		return variableType != null ? variableType.resolveTypes(compilationUnit, declaringType) : false;
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		if(variableType != null && !variableType.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		return true;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		codeGenerator.getCompilationUnit().importType(variableType);
		codeGenerator.append(variableType.getJavaNew(getStaticContext()));
	}
}
