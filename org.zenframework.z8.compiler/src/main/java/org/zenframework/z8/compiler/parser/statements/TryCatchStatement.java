package org.zenframework.z8.compiler.parser.statements;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.text.edits.TextEdit;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IStatement;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.util.Set;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class TryCatchStatement extends LanguageElement implements IStatement {
    private IToken tryToken;
    private CompoundStatement tryStatement;

    private List<CatchClause> catchClauses;

    private IToken finallyToken;
    private CompoundStatement finallyStatement;

    public TryCatchStatement(IToken tryToken, CompoundStatement tryStatement) {
        this.tryToken = tryToken;
        this.tryStatement = tryStatement;
        this.catchClauses = new ArrayList<CatchClause>();

        this.tryStatement.setParent(this);
    }

    @Override
    public IPosition getSourceRange() {
        if(finallyStatement != null) {
            return tryToken.getPosition().union(finallyStatement.getSourceRange());
        }
        else if(finallyToken != null) {
            return tryToken.getPosition().union(finallyToken.getPosition());
        }
        else if(catchClauses.size() > 0) {
            return tryToken.getPosition().union(catchClauses.get(catchClauses.size() - 1).getSourceRange());
        }
        return tryToken.getPosition().union(tryStatement.getSourceRange());
    }

    @Override
    public IToken getFirstToken() {
        return tryToken;
    }

    @Override
    public IPosition getPosition() {
        return tryToken.getPosition();
    }

    public void addCatchClause(CatchClause catchClause) {
        catchClause.setParent(this);
        catchClauses.add(catchClause);
    }

    public void setFinallyStatement(IToken finallyToken, CompoundStatement finallyStatement) {
        this.finallyStatement = finallyStatement;
        this.finallyStatement.setParent(this);
    }

    @Override
    public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
        if(!super.resolveTypes(compilationUnit, declaringType))
            return false;

        if(!tryStatement.resolveTypes(compilationUnit, declaringType))
            return false;

        Set<String> types = new Set<String>();

        for(CatchClause catchStatement : catchClauses) {
            if(!catchStatement.resolveTypes(compilationUnit, declaringType))
                return false;

            String name = catchStatement.getVariableType().getSignature();

            if(types.get(name) != null) {
                setError(catchStatement.getPosition(), "Unreachable catch block for " + name
                        + ". It is already handled by the previous catch block");
            }
            else {
                types.add(name);
            }
        }

        if(finallyStatement != null && !finallyStatement.resolveTypes(compilationUnit, declaringType))
            return false;

        return true;
    }

    @Override
    public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod,
            IVariable leftHandValue, IVariableType context) {
        if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
            return false;

        tryStatement.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);

        for(CatchClause catchStatement : catchClauses) {
            catchStatement.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
        }

        if(finallyStatement != null) {
            finallyStatement.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
        }

        return true;
    }

    @Override
    public boolean resolveNestedTypes(CompilationUnit compilationUnit, IType declaringType) {
        if(!super.resolveNestedTypes(compilationUnit, declaringType))
            return false;

        tryStatement.resolveNestedTypes(compilationUnit, declaringType);

        for(CatchClause catchStatement : catchClauses) {
            catchStatement.resolveNestedTypes(compilationUnit, declaringType);
        }

        if(finallyStatement != null) {
            finallyStatement.resolveNestedTypes(compilationUnit, declaringType);
        }

        return true;
    }

    @Override
    public boolean returnsOnAllControlPaths() {
        boolean allCatchesReturn = true;

        for(CatchClause catchClause : catchClauses) {
            assert (catchClause instanceof IStatement);
            IStatement catchStatement = (IStatement)catchClause;

            allCatchesReturn &= catchStatement.returnsOnAllControlPaths();
        }

        if(finallyStatement != null) {
            IStatement statement = (IStatement)finallyStatement;
            statement.returnsOnAllControlPaths();
        }

        IStatement statement = (IStatement)tryStatement;

        return statement.returnsOnAllControlPaths() && allCatchesReturn;
    }

    @Override
    public boolean breaksControlFlow() {
        return false;
    }

    @Override
    public void getClassCode(CodeGenerator codeGenerator) {
        tryStatement.getClassCode(codeGenerator);

        for(CatchClause catchStatement : catchClauses) {
            catchStatement.getClassCode(codeGenerator);
        }
    }

    @Override
    public void getCode(CodeGenerator codeGenerator) {
        codeGenerator.append("try");
        codeGenerator.breakLine();
        codeGenerator.indent();
        tryStatement.getCode(codeGenerator);

        for(CatchClause catchStatement : catchClauses) {
            codeGenerator.indent();
            catchStatement.getCode(codeGenerator);
        }

        if(finallyStatement != null) {
            codeGenerator.indent();
            codeGenerator.append("finally");
            codeGenerator.breakLine();
            codeGenerator.indent();
            finallyStatement.getCode(codeGenerator);
        }
    }

    @Override
    public void replaceTypeName(TextEdit parent, IType type, String newTypeName) {
        tryStatement.replaceTypeName(parent, type, newTypeName);

        for(CatchClause catchStatement : catchClauses) {
            catchStatement.replaceTypeName(parent, type, newTypeName);
        }

        if(finallyStatement != null) {
            finallyStatement.replaceTypeName(parent, type, newTypeName);
        }
    }
}
