package org.zenframework.z8.compiler.parser.statements;

import org.eclipse.text.edits.TextEdit;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IStatement;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class IfStatement extends LanguageElement implements IStatement {
	private IToken ifToken;
	@SuppressWarnings("unused")
	private IToken elseToken;

	private LoopCondition condition;
	private ILanguageElement ifStatement;
	private ILanguageElement elseStatement;

	public IfStatement(IToken ifToken, ILanguageElement condition, ILanguageElement ifStatement, IToken elseToken, ILanguageElement elseStatement) {
		this.ifToken = ifToken;
		this.condition = new LoopCondition(condition);
		this.ifStatement = ifStatement;
		this.elseToken = elseToken;
		this.elseStatement = elseStatement;

		this.ifStatement.setParent(this);

		if(this.elseStatement != null)
			this.elseStatement.setParent(this);
	}

	@Override
	public IPosition getSourceRange() {
		if(elseStatement != null)
			return ifToken.getPosition().union(elseStatement.getSourceRange());
		return ifToken.getPosition().union(ifStatement.getSourceRange());
	}

	@Override
	public IToken getFirstToken() {
		return ifToken;
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveTypes(compilationUnit, declaringType))
			return false;

		condition.resolveTypes(compilationUnit, declaringType);

		ifStatement.resolveTypes(compilationUnit, declaringType);

		if(elseStatement != null)
			elseStatement.resolveTypes(compilationUnit, declaringType);

		return true;
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		condition.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);

		declaringMethod.openLocalScope();
		ifStatement.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
		declaringMethod.closeLocalScope();

		if(elseStatement != null) {
			declaringMethod.openLocalScope();
			elseStatement.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
			declaringMethod.closeLocalScope();
		}

		return true;
	}

	@Override
	public boolean resolveNestedTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveNestedTypes(compilationUnit, declaringType))
			return false;

		ifStatement.resolveNestedTypes(compilationUnit, declaringType);

		if(elseStatement != null)
			elseStatement.resolveNestedTypes(compilationUnit, declaringType);

		return true;
	}

	@Override
	public boolean returnsOnAllControlPaths() {
		boolean result = ((IStatement)ifStatement).returnsOnAllControlPaths();

		if(elseStatement != null)
			return result && ((IStatement)elseStatement).returnsOnAllControlPaths();

		return false;
	}

	@Override
	public boolean breaksControlFlow() {
		return false;
	}

	@Override
	public void getClassCode(CodeGenerator codeGenerator) {
		ifStatement.getClassCode(codeGenerator);

		if(elseStatement != null)
			elseStatement.getClassCode(codeGenerator);
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		codeGenerator.getCompilationUnit().setLineNumbers(getSourceRange().getLine(), codeGenerator.getCurrentLine());

		codeGenerator.append("if(");
		condition.getCode(codeGenerator);
		codeGenerator.append(")");
		codeGenerator.breakLine();

		boolean braces = ifStatement instanceof CompoundStatement;

		if(!braces)
			codeGenerator.incrementIndent();

		codeGenerator.indent();
		ifStatement.getCode(codeGenerator);

		if(!braces)
			codeGenerator.decrementIndent();

		if(elseStatement == null)
			return;

		braces = elseStatement instanceof CompoundStatement;

		codeGenerator.indent();
		codeGenerator.append("else");

		if(elseStatement instanceof IfStatement) {
			codeGenerator.append(" ");
			elseStatement.getCode(codeGenerator);
			return;
		}

		codeGenerator.breakLine();

		if(!braces)
			codeGenerator.incrementIndent();

		codeGenerator.indent();
		elseStatement.getCode(codeGenerator);

		if(!braces)
			codeGenerator.decrementIndent();
	}

	@Override
	public void replaceTypeName(TextEdit parent, IType type, String newTypeName) {
		condition.replaceTypeName(parent, type, newTypeName);

		ifStatement.replaceTypeName(parent, type, newTypeName);

		if(elseStatement != null)
			elseStatement.replaceTypeName(parent, type, newTypeName);
	}
}
