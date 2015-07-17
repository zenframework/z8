package org.zenframework.z8.compiler.parser.statements;

import org.eclipse.text.edits.TextEdit;

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
    public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod,
            IVariable leftHandValue, IVariableType context) {
        if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
            return false;

        declaringMethod.openLocalScope();

        if(declarator != null) {
            declarator.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
        }

        IType exceptionType = Primary.resolveType(compilationUnit, Primary.Exception);

        if(exceptionType != null) {
            IVariableType variableType = declarator.getVariableType();

            ITypeCast typeCast = variableType.getCastTo(exceptionType);

            if(typeCast == null || typeCast.getOperator() != null) {
                setError(declarator.getPosition(), "Type mismatch: cannot convert from " + variableType.getSignature()
                        + " to " + exceptionType.getUserName());
            }
        }

        if(statement != null) {
            statement.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
        }

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
        codeGenerator.getCompilationUnit().importType(type);
        codeGenerator.append("catch(" + type.getJavaName() + " " + declarator.getName() + ")");
        codeGenerator.breakLine();
        codeGenerator.indent();
        statement.getCode(codeGenerator);
    }

    @Override
    public void replaceTypeName(TextEdit parent, IType type, String newTypeName) {
        statement.replaceTypeName(parent, type, newTypeName);
    }
}
