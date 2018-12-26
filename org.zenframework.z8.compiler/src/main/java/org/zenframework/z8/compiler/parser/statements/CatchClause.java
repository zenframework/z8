package org.zenframework.z8.compiler.parser.statements;

import org.zenframework.z8.compiler.core.CodeGenerator;
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

public class CatchClause extends LanguageElement implements IStatement {
	private IToken catchToken;

	private Declarator declarator;
	private CompoundStatement statement;

	public CatchClause(IToken catchToken, Declarator declarator, CompoundStatement statement) {
		this.catchToken = catchToken;
		this.declarator = declarator;
		this.statement = statement;

		this.statement.setParent(this);
	}

	@Override
	public IPosition getSourceRange() {
		return catchToken.getPosition().union(statement.getSourceRange());
	}

	@Override
	public IToken getFirstToken() {
		return catchToken;
	}

	@Override
	public IVariableType getVariableType() {
		return declarator.getVariableType();
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveTypes(compilationUnit, declaringType))
			return false;

		declarator.resolveTypes(compilationUnit, declaringType);
		statement.resolveTypes(compilationUnit, declaringType);
		return true;
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		declaringMethod.openLocalScope();

		if(declarator != null)
			declarator.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);

		IType exceptionType = Primary.resolveType(compilationUnit, Primary.Exception);

		if(exceptionType != null) {
			IVariableType variableType = declarator.getVariableType();

			ITypeCast typeCast = variableType.getCastTo(exceptionType);

			if(typeCast == null || typeCast.getOperator() != null)
				setError(declarator.getPosition(), "Type mismatch: cannot convert from " + variableType.getSignature() + " to " + exceptionType.getUserName());
		}

		if(statement != null)
			statement.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);

		declaringMethod.closeLocalScope();

		return true;
	}

	public boolean catchesAll() {
		return declarator.getVariableType().getType().isPrimary();
	}

	@Override
	public boolean returnsOnAllControlPaths() {
		return ((IStatement)statement).returnsOnAllControlPaths();
	}

	@Override
	public boolean breaksControlFlow() {
		return false;
	}

	@Override
	public void getClassCode(CodeGenerator codeGenerator) {
		statement.getClassCode(codeGenerator);
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		IType type = getVariableType().getType();

		CompilationUnit compilationUnit = codeGenerator.getCompilationUnit();
		compilationUnit.importType(type);

		String typeName = type.getJavaName();
		String declaration = typeName + " " + declarator.getName();

		if(type.isPrimary()) {
			String throwableName = compilationUnit.createUniqueName();
			codeGenerator.append("catch(java.lang.Throwable " + throwableName + ") {");
			codeGenerator.breakLine().incrementIndent().indent();

			codeGenerator.append(declaration + " = new " + typeName + "(" + throwableName + ");");
			codeGenerator.breakLine().indent();

			statement.getCode(codeGenerator);

			codeGenerator.decrementIndent().indent().append('}').breakLine();
		} else {
			codeGenerator.append("catch(" + declaration + ")").breakLine().indent();
			statement.getCode(codeGenerator);
		}
	}
}
