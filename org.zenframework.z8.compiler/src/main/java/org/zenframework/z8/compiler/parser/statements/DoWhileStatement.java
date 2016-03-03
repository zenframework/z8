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
import org.zenframework.z8.compiler.parser.expressions.BracedExpression;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class DoWhileStatement extends LanguageElement implements IStatement {
    private IToken doToken;
    private ILanguageElement statement;
    @SuppressWarnings("unused")
    private IToken whileToken;
    private LoopCondition condition;

    public DoWhileStatement(IToken doToken, ILanguageElement statement, IToken whileToken, BracedExpression expression) {
        this.doToken = doToken;
        this.statement = statement;
        this.whileToken = whileToken;
        this.condition = new LoopCondition(expression);

        this.statement.setParent(this);
    }

    @Override
    public IPosition getPosition() {
        return doToken.getPosition();
    }

    @Override
    public IPosition getSourceRange() {
        return doToken.getPosition().union(condition.getSourceRange());
    }

    @Override
    public IToken getFirstToken() {
        return doToken;
    }

    @Override
    public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
        return super.resolveTypes(compilationUnit, declaringType)
                && condition.resolveTypes(compilationUnit, declaringType)
                && statement.resolveTypes(compilationUnit, declaringType);
    }

    @Override
    public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod,
            IVariable leftHandValue, IVariableType context) {
        if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
            return false;

        declaringMethod.openLocalScope();
        statement.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
        declaringMethod.closeLocalScope();

        condition.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);

        return true;
    }

    @Override
    public boolean resolveNestedTypes(CompilationUnit compilationUnit, IType declaringType) {
        return statement.resolveNestedTypes(compilationUnit, declaringType);
    }

    @Override
    public boolean returnsOnAllControlPaths() {
        ((IStatement)statement).returnsOnAllControlPaths();
        return false;
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
        codeGenerator.append("do");
        codeGenerator.breakLine();

        boolean braces = statement instanceof CompoundStatement;

        if(!braces)
            codeGenerator.incrementIndent();

        codeGenerator.indent();
        statement.getCode(codeGenerator);

        if(!braces)
            codeGenerator.decrementIndent();

        codeGenerator.indent();
        codeGenerator.append("while(");
        condition.getCode(codeGenerator);
        codeGenerator.append(")");
        codeGenerator.append(";");
        codeGenerator.breakLine();
    }

    @Override
    public void replaceTypeName(TextEdit parent, IType type, String newTypeName) {
        statement.replaceTypeName(parent, type, newTypeName);
        condition.replaceTypeName(parent, type, newTypeName);
    }
}
