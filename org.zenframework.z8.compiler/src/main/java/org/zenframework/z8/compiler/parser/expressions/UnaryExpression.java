package org.zenframework.z8.compiler.parser.expressions;

import java.util.ArrayList;
import java.util.List;

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
import org.zenframework.z8.compiler.parser.grammar.lexer.token.OperatorToken;
import org.zenframework.z8.compiler.parser.type.TypeCast;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class UnaryExpression extends LanguageElement {
	private OperatorToken operatorToken;
	private ILanguageElement expression;

	private IMethod operator;
	private ITypeCast typeCast;

	public UnaryExpression(OperatorToken operatorToken, ILanguageElement expression) {
		this.operatorToken = operatorToken;
		this.expression = expression;
	}

	@Override
	public IPosition getSourceRange() {
		if(expression != null) {
			return operatorToken.getPosition().union(expression.getPosition());
		}
		return operatorToken.getPosition();
	}

	@Override
	public IToken getFirstToken() {
		return operatorToken;
	}

	public OperatorToken getOperatorToken() {
		return operatorToken;
	}

	public ILanguageElement getExpression() {
		return expression;
	}

	@Override
	public IVariableType getVariableType() {
		return operator.getVariableType();
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveTypes(compilationUnit, declaringType))
			return false;

		return expression.resolveTypes(compilationUnit, declaringType);
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		if(expression == null || !expression.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		IVariableType expressionType = expression.getVariableType();

		IMethod[] operators = expressionType.getMatchingMethods(operatorToken.getName());

		for(IMethod operator : operators) {
			IVariable[] parameters = operator.getParameters();

			if(parameters.length == 0) {
				typeCast = new TypeCast(expressionType, expressionType, 0);
				this.operator = operator;
				return true;
			}
		}

		List<ITypeCast> typeCastCandidates = new ArrayList<ITypeCast>();
		List<IMethod> operatorCandidates = new ArrayList<IMethod>();

		IMethod[] typeCastOperators = expressionType.getTypeCastOperators();

		for(IMethod typeCastOperator : typeCastOperators) {
			IVariableType variableType = typeCastOperator.getVariableType();

			operators = variableType.getMatchingMethods(operatorToken.getName());

			for(IMethod operator : operators) {
				IVariable[] parameters = operator.getParameters();

				if(parameters.length == 0) {
					typeCastCandidates.add(new TypeCast(expressionType, typeCastOperator));
					operatorCandidates.add(operator);
					break;
				}
			}
		}

		if(operatorCandidates.size() == 0) {
			setError(getPosition(), "The " + operatorToken.getName() + " is undefined for the argument type " + expressionType.getSignature());
			return false;
		} else if(operatorCandidates.size() != 1) {
			setError(getPosition(), "The " + operatorToken.getName() + " is ambiguous for the type " + expressionType.getSignature());
			return false;
		}

		operator = operatorCandidates.get(0);
		typeCast = typeCastCandidates.get(0);

		return true;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		typeCast.getCode(codeGenerator, expression);

		codeGenerator.append('.');

		if(typeCast.getTarget().isReference()) {
			codeGenerator.append("get().");
		}

		codeGenerator.append(operator.getJavaName() + "()");
	}
}
