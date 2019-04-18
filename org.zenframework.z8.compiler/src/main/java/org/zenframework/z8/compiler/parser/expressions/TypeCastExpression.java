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
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class TypeCastExpression extends LanguageElement {
	private IToken leftBrace;
	private IToken rightBrace;
	private IVariableType variableType;
	private ILanguageElement expression;

	private ITypeCast typeCast;
	private ITypeCast typeDowncast;

	public TypeCastExpression(IToken leftBrace, QualifiedName typeName, IToken rightBrace) {
		this.leftBrace = leftBrace;
		this.variableType = new VariableType(typeName);
		this.rightBrace = rightBrace;
	}

	public TypeCastExpression(IVariableType variableType, ILanguageElement expression) {
		this.variableType = variableType;
		this.expression = expression;
	}

	@Override
	public IToken getFirstToken() {
		return leftBrace != null ? leftBrace : variableType.getFirstToken();
	}

	public void setExpression(ILanguageElement expression) {
		this.expression = expression;
	}

	@Override
	public IPosition getSourceRange() {
		return leftBrace != null ? leftBrace.getPosition().union(rightBrace.getPosition()) : variableType.getPosition();
	}

	@Override
	public IVariableType getVariableType() {
		return variableType;
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		return super.resolveTypes(compilationUnit, declaringType) && variableType.resolveTypes(compilationUnit, declaringType) && expression.resolveTypes(compilationUnit, declaringType);
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null) ||
				!variableType.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null) ||
				!expression.checkSemantics(compilationUnit, declaringType, declaringMethod, new Variable(variableType), null))
			return false;

		typeCast = expression.getVariableType().getCastTo(variableType);

		if(typeCast == null) {
			ITypeCast downcast = variableType.getCastTo(expression.getVariableType());
			typeDowncast = downcast != null && downcast.isBaseTypeCast() ? downcast : null;
		}

		if(typeCast == null && typeDowncast == null) {
			setError(getPosition(), "Type mismatch: cannot convert from " + expression.getVariableType().getSignature() + " to " + getVariableType().getSignature());
			return false;
		}

		return true;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		if(typeDowncast != null) {
			codeGenerator.getCompilationUnit().importType(variableType);

			codeGenerator.append('(');
			codeGenerator.append('(' + variableType.getJavaName() + ')');
			expression.getCode(codeGenerator);
			codeGenerator.append(')');
		} else if(typeCast != null)
			typeCast.getCode(codeGenerator, expression);
	}
}
