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

public abstract class Initialization extends LanguageElement {
	private ILanguageElement left;
	private ILanguageElement right;
	private OperatorToken operatorToken;

	private ITypeCast typeCast;

	protected Initialization(ILanguageElement left) {
		this.left = left;
	}

	protected Initialization(ILanguageElement left, OperatorToken operatorToken, ILanguageElement right) {
		this.left = left;
		this.right = right;
		this.operatorToken = operatorToken != null ? new OperatorToken(IToken.ASSIGN, operatorToken.getPosition()) : null;
	}

	public ILanguageElement getLeftElement() {
		return left;
	}

	public ILanguageElement getRightElement() {
		return right;
	}

	public IToken getOperator() {
		return operatorToken;
	}

	@Override
	public IPosition getSourceRange() {
		return right != null ? left.getSourceRange().union(right.getSourceRange()) : left.getSourceRange();
	}

	@Override
	public IToken getFirstToken() {
		return getFirstToken(super.getFirstToken(), left.getFirstToken());
	}

	@Override
	public IVariableType getVariableType() {
		return left.getVariableType();
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveTypes(compilationUnit, declaringType))
			return false;

		if(!left.resolveTypes(compilationUnit, declaringType))
			return false;

		if(right != null && !right.resolveTypes(compilationUnit, declaringType))
			return false;

		return true;
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		if(!left.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		if(right == null)
			return true;

		leftHandValue = left.getVariable();

		right.setStaticContext(getStaticContext());

		if(!right.checkSemantics(compilationUnit, declaringType, declaringMethod, leftHandValue, null))
			return false;

		if(left instanceof QualifiedName) {
			QualifiedName qname = (QualifiedName)left;
			IVariableType type = qname.getVariableType();

			if(qname.getTokenCount() == 1 && type.isStatic()) {
				setFatalError(qname.getPosition(), type.getSignature() + " cannot be resolved");
				return false;
			}
		}

		IVariableType type = leftHandValue.getVariableType();
		IVariableType argument = right.getVariableType();

		List<ITypeCast> candidates = new ArrayList<ITypeCast>();

		IMethod[] operators = type.getMatchingMethods(operatorToken.getName());

		for(IMethod operator : operators) {
			IVariable[] parameters = operator.getParameters();

			if(parameters.length != 1)
				continue;

			IVariableType parameter = parameters[0].getVariableType();

			ITypeCast typeCast = argument.getCastTo(parameter);

			if(typeCast != null) {
				typeCast.setContext(operator);
				candidates.add(typeCast);
			}
		}

		if(candidates.size() != 0) {
			ITypeCast[] result = TypeCast.findBestCast(candidates.toArray(new ITypeCast[candidates.size()]));

			if(result.length == 1) {
				typeCast = result[0];
			} else {
				setError(getPosition(), "The " + result[0].getContext().getSignature() + " is ambiguous for the type " + type.getSignature());
				return false;
			}
		} else if(operatorToken.getId() == IToken.ASSIGN) {
			typeCast = right.getVariableType().getCastTo(leftHandValue.getVariableType());
		} else {
			setError(getPosition(), "The " + operatorToken.getName() + " is undefined for the argument type(s) " + left.getVariableType().getSignature() + ", " + right.getVariableType().getSignature());
			return false;
		}

		if(typeCast == null) {
			setError(getPosition(), "Type mismatch: cannot convert from " + right.getVariableType().getSignature() + " to " + leftHandValue.getVariableType().getSignature());
			return false;
		}

		return true;
	}

	public boolean requiresAllocation() {
		return right == null || typeCast.getContext() != null;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		if(right != null) {
			getLeftSideCode(codeGenerator);
			getRightSideCode(codeGenerator);
		}
	}

	protected void getLeftSideCode(CodeGenerator codeGenerator) {
		if(typeCast.getContext() != null) {
			left.getCode(codeGenerator);

			codeGenerator.append('.');

			if(getVariableType().isReference())
				codeGenerator.append("get(" + getDeclaringType().getConstructionStage() + ").");
		} else {
			left.getCode(codeGenerator);
			codeGenerator.append(" = ");
		}
	}

	protected void getRightSideCode(CodeGenerator codeGenerator) {
		typeCast.getCode(codeGenerator, right);
	}
}
