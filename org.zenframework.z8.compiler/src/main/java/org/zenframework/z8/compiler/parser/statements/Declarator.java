package org.zenframework.z8.compiler.parser.statements;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IStatement;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.expressions.Initialization;
import org.zenframework.z8.compiler.parser.expressions.QualifiedName;
import org.zenframework.z8.compiler.parser.grammar.lexer.Lexer;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.OperatorToken;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class Declarator extends Initialization implements IVariable, IStatement {
	private IToken finalToken;
	private VariableType variableType;
	private int closure = -1;

	public Declarator(IToken finalToken, VariableType variableType, IToken nameToken, OperatorToken operatorToken, ILanguageElement initializer) {
		super(new QualifiedName(nameToken), operatorToken, initializer);
		this.variableType = variableType;
		this.finalToken = finalToken;
	}

	@Override
	public IPosition getSourceRange() {
		return variableType.getSourceRange().union(super.getSourceRange());
	}

	@Override
	public IPosition getPosition() {
		return getLeftElement().getPosition();
	}

	@Override
	public IToken getFirstToken() {
		return getFirstToken(getFirstToken(super.getFirstToken(), finalToken), variableType.getFirstToken());
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	@Override
	public int getClosure() {
		return closure;
	}

	@Override
	public void setClosure(int closure) {
		this.closure = closure;
	}

	@Override
	public IVariableType getVariableType() {
		return variableType;
	}

	@Override
	public boolean isFinal() {
		return finalToken != null;
	}

	public IToken getNameToken() {
		return ((QualifiedName)getLeftElement()).getFirstToken();
	}

	@Override
	public String getName() {
		return ((QualifiedName)getLeftElement()).toString();
	}

	@Override
	public String getJavaName() {
		return getName();
	}

	@Override
	public String getUserName() {
		return getName() + " " + getSignature();
	}

	@Override
	public String getSignature() {
		return getVariableType().getSignature();
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!variableType.resolveTypes(compilationUnit, declaringType))
			return false;

		if(!super.resolveTypes(compilationUnit, declaringType))
			return false;

		String name = getName();

		if(!Lexer.checkIdentifier(getName())) {
			setFatalError(getNameToken().getPosition(), "Syntax error on token '" + name + "'. " + name + " is a reserved keyword.");
			return false;
		}

		compilationUnit.addHyperlink(getPosition(), compilationUnit, getPosition());
		return true;
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(variableType.hasFatalError())
			return false;

		if(!variableType.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		IVariable variable = declaringMethod.findLocalVariable(getName());

		if(variable != null) {
			setFatalError(getPosition(), getName() + ": redefinition");
			return false;
		}

		declaringMethod.addLocalVariable(this);

		return super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
	}

	@Override
	public boolean returnsOnAllControlPaths() {
		return false;
	}

	@Override
	public boolean breaksControlFlow() {
		return false;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		getCode(codeGenerator, false);
	}

	public void getCode(CodeGenerator codeGenerator, boolean declareOnly) {
		codeGenerator.getCompilationUnit().setLineNumbers(getSourceRange().getLine(), codeGenerator.getCurrentLine());

		IVariableType variableType = getVariableType();

		codeGenerator.getCompilationUnit().importType(variableType.getType());
		codeGenerator.append(variableType.getDeclaringJavaName() + " ");

		if(declareOnly) {
			codeGenerator.append(getJavaName());
			return;
		}

		if(requiresAllocation()) {
			codeGenerator.append(getJavaName() + " = " + variableType.getJavaNew(getStaticContext()));

			ILanguageElement right = getRightElement();

			if(right != null) {
				codeGenerator.append(";");
				codeGenerator.breakLine();
				codeGenerator.indent();
			}
		}

		super.getCode(codeGenerator);
	}
}
