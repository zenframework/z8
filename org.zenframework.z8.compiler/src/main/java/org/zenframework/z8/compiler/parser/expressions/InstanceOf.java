package org.zenframework.z8.compiler.parser.expressions;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.type.Primary;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class InstanceOf extends LanguageElement {
	private ILanguageElement left;
	@SuppressWarnings("unused")
	private IToken token;
	private IVariableType right;

	private IType booleanType;

	public InstanceOf(ILanguageElement left, IToken token, IVariableType right) {
		this.left = left;
		this.token = token;
		this.right = right;
	}

	@Override
	public IPosition getSourceRange() {
		return left.getSourceRange().union(right.getSourceRange());
	}

	@Override
	public IToken getFirstToken() {
		return left.getFirstToken();
	}

	@Override
	public IVariableType getVariableType() {
		return booleanType.getVariableType();
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveTypes(compilationUnit, declaringType))
			return false;

		booleanType = Primary.resolveType(compilationUnit, Primary.Boolean);

		return left.resolveTypes(compilationUnit, declaringType) && right.resolveTypes(compilationUnit, declaringType);
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		if(right.hasFatalError())
			return false;

		boolean result = left.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
		return right.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null) && result;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		IType type = right.getType();
		String javaName = type.isQualified() ? type.getQualifiedJavaName() : type.getJavaName();

		codeGenerator.getCompilationUnit().importType(booleanType);
		codeGenerator.getCompilationUnit().importType(right.getType());

		codeGenerator.append("new bool(");
		left.getCode(codeGenerator);
		codeGenerator.append(" instanceof " + javaName + (type.isPrimary() ? "" : ".CLASS")).append(")");
	}
}
