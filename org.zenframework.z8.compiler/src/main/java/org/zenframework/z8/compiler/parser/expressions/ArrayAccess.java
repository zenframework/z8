package org.zenframework.z8.compiler.parser.expressions;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IJavaTypeCast;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.ITypeCast;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.type.Primary;
import org.zenframework.z8.compiler.parser.variable.LeftHandValue;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class ArrayAccess extends LanguageElement implements IJavaTypeCast {
	private ILanguageElement array;
	private ILanguageElement expression;

	@SuppressWarnings("unused")
	private IToken leftBracket;
	private IToken rightBracket;

	private ITypeCast typeCast;

	private boolean javaCastPending = true;

	public ArrayAccess(ILanguageElement array, ILanguageElement expression, IToken leftBracket, IToken rightBracket) {
		this.array = array;
		this.expression = expression;
		this.leftBracket = leftBracket;
		this.rightBracket = rightBracket;
	}

	@Override
	public IPosition getSourceRange() {
		if(rightBracket != null)
			return array.getPosition().union(rightBracket.getPosition());
		return array.getPosition().union(expression.getSourceRange());
	}

	@Override
	public IToken getFirstToken() {
		return array.getFirstToken();
	}

	@Override
	public IVariable getVariable() {
		return new LeftHandValue(this);
	}

	@Override
	public IVariableType getVariableType() {
		IVariableType variableType = new VariableType(array.getVariableType());
		variableType.removeRightKey();
		return variableType;
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		return super.resolveTypes(compilationUnit, declaringType) && array.resolveTypes(compilationUnit, declaringType) && expression.resolveTypes(compilationUnit, declaringType);
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		if(!array.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		if(!array.getVariableType().isArray()) {
			setError(array.getPosition(), "The type of the expression must be an array type but it resolved to " + array.getVariableType().getSignature());
			return false;
		}

		if(!expression.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		IType index = array.getVariableType().getRightKey();

		if(index == null)
			index = Primary.resolveType(compilationUnit, Primary.Integer);

		if(index != null) {
			typeCast = expression.getVariableType().getCastTo(index);

			if(typeCast == null) {
				setError(expression.getPosition(), "Type mismatch: cannot convert from " + expression.getVariableType().getSignature() + " to " + index.getUserName());
				return false;
			}
		} else
			return false;

		return true;
	}

	@Override
	public void setCastPending(boolean castPending) {
		javaCastPending = castPending;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		IVariableType variableType = getVariableType();

		if(javaCastPending && !variableType.isArray()) {
			codeGenerator.getCompilationUnit().importType(getVariableType().getType());
			codeGenerator.append("((" + getVariableType().getDeclaringJavaName() + ")");
		}

		array.getCode(codeGenerator);
		codeGenerator.append(".get(");
		typeCast.getCode(codeGenerator, expression);
		codeGenerator.append(")");

		if(javaCastPending && !variableType.isArray())
			codeGenerator.append(")");
	}
}
