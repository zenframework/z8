package org.zenframework.z8.compiler.parser.statements;

import org.eclipse.text.edits.TextEdit;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IStatement;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.ITypeCast;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.type.Primary;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class ThrowStatement extends LanguageElement implements IStatement {
	private IToken throwToken;
	private ILanguageElement expression;

	private ITypeCast typeCast;

	public ThrowStatement(IToken throwToken, ILanguageElement expression) {
		this.throwToken = throwToken;
		this.expression = expression;
	}

	@Override
	public IPosition getSourceRange() {
		return throwToken.getPosition().union(expression.getPosition());
	}

	@Override
	public IToken getFirstToken() {
		return throwToken;
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		return super.resolveTypes(compilationUnit, declaringType) && expression.resolveTypes(compilationUnit, declaringType);
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		if(!expression.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		IType exceptionType = Primary.resolveType(compilationUnit, Primary.Exception);

		if(exceptionType != null) {
			IVariableType type = expression.getVariableType();

			typeCast = type.getCastTo(exceptionType);

			if(typeCast == null) {
				setError(getPosition(), "Type mismatch: cannot conver from " + type.getSignature() + " to " + exceptionType.getUserName());
				return false;
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean returnsOnAllControlPaths() {
		return true;
	}

	@Override
	public boolean breaksControlFlow() {
		return true;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		codeGenerator.getCompilationUnit().setLineNumbers(getSourceRange().getLine(), codeGenerator.getCurrentLine());

		codeGenerator.append("throw ");

		typeCast.getCode(codeGenerator, expression);
		codeGenerator.append(';');

		codeGenerator.breakLine();
	}

	@Override
	public void replaceTypeName(TextEdit parent, IType type, String newTypeName) {
		expression.replaceTypeName(parent, type, newTypeName);
	}
}
