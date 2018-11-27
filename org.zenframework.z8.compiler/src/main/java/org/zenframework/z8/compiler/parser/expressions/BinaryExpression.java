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
import org.zenframework.z8.compiler.parser.type.Primary;
import org.zenframework.z8.compiler.parser.type.TypeCast;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class BinaryExpression extends LanguageElement {
	private ILanguageElement left;
	private ILanguageElement right;
	private OperatorToken operatorToken;

	private ITypeCast leftTypeCast;
	private ITypeCast rightTypeCast;

	public BinaryExpression(ILanguageElement left, OperatorToken operatorToken, ILanguageElement right) {
		this.left = left;
		this.right = right;
		this.operatorToken = operatorToken;
	}

	@Override
	public IPosition getSourceRange() {
		return left.getPosition().union(right.getPosition());
	}

	@Override
	public IToken getFirstToken() {
		return left.getFirstToken();
	}

	@Override
	public IVariableType getVariableType() {
		return rightTypeCast.getContext().getVariableType();
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		return super.resolveTypes(compilationUnit, declaringType) && left.resolveTypes(compilationUnit, declaringType) && right.resolveTypes(compilationUnit, declaringType);
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		boolean ok = left.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);

		if(!right.checkSemantics(compilationUnit, declaringType, declaringMethod, left.getVariable(), null) || !ok)
			return false;

		IVariableType leftType = left.getVariableType();
		IVariableType rightType = right.getVariableType();

		IMethod[] operators = leftType.getMatchingMethods(operatorToken.getName());

		List<ITypeCast> rightTypeCastCandidates = new ArrayList<ITypeCast>();
		List<ITypeCast> leftTypeCastCandidates = new ArrayList<ITypeCast>();

		for(IMethod operator : operators) {
			IVariable[] parameters = operator.getParameters();

			if(parameters.length != 1)
				continue;

			IVariableType parameter = parameters[0].getVariableType();

			ITypeCast typeCast = rightType.getCastTo(parameter);

			if(typeCast != null) {
				typeCast.setContext(operator);
				rightTypeCastCandidates.add(typeCast);
			}
		}

		ITypeCast[] result = TypeCast.findBestCast(rightTypeCastCandidates.toArray(new ITypeCast[rightTypeCastCandidates.size()]));

		if(result.length == 1) {
			rightTypeCast = result[0];
			leftTypeCast = new TypeCast(leftType, leftType, 0);
			return true;
		} else if(result.length != 0) {
			setError(getPosition(), "The " + operatorToken.getName() + " is ambiguous for the type(s) " + leftType.getSignature() + ", " + rightType.getSignature());
			return false;
		}

		IMethod[] typeCastOperators = leftType.getTypeCastOperators();

		for(IMethod typeCastOperator : typeCastOperators) {
			IVariableType variableType = typeCastOperator.getVariableType();

			operators = variableType.getMatchingMethods(operatorToken.getName());

			List<ITypeCast> candidates = new ArrayList<ITypeCast>();

			for(IMethod operator : operators) {
				IVariable[] parameters = operator.getParameters();

				if(parameters.length != 1)
					continue;

				IVariableType parameter = parameters[0].getVariableType();

				ITypeCast typeCast = rightType.getCastTo(parameter);

				if(typeCast != null) {
					typeCast.setContext(operator);
					candidates.add(typeCast);
				}
			}

			result = TypeCast.findBestCast(candidates.toArray(new ITypeCast[candidates.size()]));

			if(result.length == 0)
				continue;

			if(result.length == 1) {
				rightTypeCastCandidates.add(result[0]);
				leftTypeCastCandidates.add(new TypeCast(leftType, typeCastOperator));
			} else if(result.length != 0) {
				setError(getPosition(), "The " + operatorToken.getName() + " is ambiguous for the type(s) " + leftType.getSignature() + ", " + rightType.getSignature());
				return false;
			}
		}

		if(rightTypeCastCandidates.size() == 0) {
			setError(getPosition(), "The " + operatorToken.getName() + " is undefined for the argument type(s) " + leftType.getSignature() + ", " + rightType.getSignature());
			return false;
		} else if(rightTypeCastCandidates.size() != 1) {
			setError(getPosition(), "The " + operatorToken.getName() + " is ambiguous for the type(s) " + leftType.getSignature() + ", " + rightType.getSignature());
			return false;
		}

		rightTypeCast = rightTypeCastCandidates.get(0);
		leftTypeCast = leftTypeCastCandidates.get(0);

		return true;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		IType booleanType = Primary.resolveType(getCompilationUnit(), Primary.Boolean);

		if(left.getVariableType().isEnum()) {
			codeGenerator.getCompilationUnit().importType(booleanType);
			codeGenerator.append("new " + booleanType.getJavaName() + "(");
			left.getCode(codeGenerator);
			codeGenerator.append(operatorToken.getSign());
			right.getCode(codeGenerator);
			codeGenerator.append(")");
		} else {
			IVariableType leftType = leftTypeCast.getTarget();
			IVariableType rightType = rightTypeCast.getTarget();

			boolean isBoolean = !leftType.isArray() && leftType.getType() == booleanType && !rightType.isArray() && rightType.getType() == booleanType;

			if(isBoolean && (operatorToken.getId() == IToken.AND || operatorToken.getId() == IToken.OR)) {
				codeGenerator.append("(");

				leftTypeCast.getCode(codeGenerator, left);
				codeGenerator.append(".get()");
				codeGenerator.append(" ? ");

				codeGenerator.getCompilationUnit().importType(booleanType);

				String trueValue = "new " + booleanType.getJavaName() + "(true)";
				String falseValue = "new " + booleanType.getJavaName() + "(false)";

				if(operatorToken.getId() == IToken.AND) {
					if(rightTypeCast.getContext() != null) {
						codeGenerator.append(trueValue);
						codeGenerator.append('.');
					}

					rightTypeCast.getCode(codeGenerator, right);
					codeGenerator.append(" : ");
					codeGenerator.append(falseValue);
				} else {
					codeGenerator.append(trueValue);
					codeGenerator.append(" : ");

					if(rightTypeCast.getContext() != null) {
						codeGenerator.append(falseValue);
						codeGenerator.append('.');
					}

					rightTypeCast.getCode(codeGenerator, right);
				}

				codeGenerator.append(')');
			} else {
				leftTypeCast.getCode(codeGenerator, left);

				codeGenerator.append('.');

				if(rightTypeCast.getTarget().isReference())
					codeGenerator.append("get().");

				rightTypeCast.getCode(codeGenerator, right);
			}
		}
	}

	public ILanguageElement getLeftElement() {
		return left;
	}

	public ILanguageElement getRightElement() {
		return right;
	}
}
