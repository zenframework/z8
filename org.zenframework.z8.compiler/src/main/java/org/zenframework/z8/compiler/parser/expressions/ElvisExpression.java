package org.zenframework.z8.compiler.parser.expressions;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.ITypeCast;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.variable.Variable;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class ElvisExpression extends LanguageElement {
	private ILanguageElement left;
	private ILanguageElement right;

	private ITypeCast rightToLeftTypeCast;

	public ElvisExpression(ILanguageElement left, ILanguageElement right) {
		this.left = left;
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
		return left.getVariableType();
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveTypes(compilationUnit, declaringType))
			return false;

		boolean result = left.resolveTypes(compilationUnit, declaringType);
		return right.resolveTypes(compilationUnit, declaringType) && result;
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		boolean result = left.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
		result = right.checkSemantics(compilationUnit, declaringType, declaringMethod, result ? new Variable(getVariableType()) : null, null) && result;

		if(!result)
			return false;

		rightToLeftTypeCast = right.getVariableType().getCastTo(left.getVariableType());

		if(rightToLeftTypeCast != null)
			return true;

		IPosition position = left.getPosition().union(right.getPosition());
		setError(position, "Operator ?: : types " + left.getVariableType().getSignature() + " and " + right.getVariableType().getSignature() + " are not compatible");

		return false;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		IVariableType variableType = getVariableType();

		codeGenerator.getCompilationUnit().importType(variableType.getType());

		codeGenerator.append("((" + variableType.getJavaName() + ")");
		codeGenerator.append("OBJECT.elvisOperator(");

		left.getCode(codeGenerator);

		codeGenerator.append(", ");

		if(rightToLeftTypeCast != null)
			rightToLeftTypeCast.getCode(codeGenerator, right);
		else
			right.getCode(codeGenerator);

		codeGenerator.append("))");
}
}
