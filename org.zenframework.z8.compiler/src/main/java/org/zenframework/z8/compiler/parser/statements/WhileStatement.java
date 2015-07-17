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

public class WhileStatement extends LanguageElement implements IStatement {
    private IToken whileToken;
    private LoopCondition condition;
    private ILanguageElement statement;

    public WhileStatement(IToken whileToken, BracedExpression expression, ILanguageElement statement) {
        this.whileToken = whileToken;
        this.condition = new LoopCondition(expression);
        this.statement = statement;

        this.statement.setParent(this);
    }

    @Override
    public IPosition getSourceRange() {
        if(statement != null) {
            return whileToken.getPosition().union(statement.getSourceRange());
        }
        else if(condition != null) {
            return whileToken.getPosition().union(condition.getSourceRange());
        }
        else {
            return whileToken.getPosition();
        }
    }

    @Override
    public IToken getFirstToken() {
        return whileToken;
    }

    public LoopCondition getCondition() {
        return condition;
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

        condition.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);

        declaringMethod.openLocalScope();
        statement.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
        declaringMethod.closeLocalScope();

        return true;
    }

    @Override
    public boolean resolveNestedTypes(CompilationUnit compilationUnit, IType declaringType) {
        return super.resolveNestedTypes(compilationUnit, declaringType)
                && statement.resolveNestedTypes(compilationUnit, declaringType);
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
    public void getClassCode(CodeGenerator codeGenerator) {
        statement.getClassCode(codeGenerator);
    }

    @Override
    public void getCode(CodeGenerator codeGenerator) {
        codeGenerator.append("while(");
        condition.getCode(codeGenerator);
        codeGenerator.append(")");
        codeGenerator.breakLine();

        boolean braces = statement instanceof CompoundStatement;

        if(!braces)
            codeGenerator.incrementIndent();

        codeGenerator.indent();
        statement.getCode(codeGenerator);

        if(!braces)
            codeGenerator.decrementIndent();
    }

    @Override
    public void replaceTypeName(TextEdit parent, IType type, String newTypeName) {
        condition.replaceTypeName(parent, type, newTypeName);
        statement.replaceTypeName(parent, type, newTypeName);
    }
}
