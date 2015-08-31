package org.zenframework.z8.compiler.parser.type.members;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.KeywordToken;
import org.zenframework.z8.compiler.parser.variable.Variable;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class PriorityOperator extends AbstractMethod {
    static public final String Name = "operatorPriority";

    private KeywordToken operatorKeyword;
    private IPosition position;

    public PriorityOperator(KeywordToken operatorKeyword, Variable[] parameters, IToken leftBrace, IToken rightBrace) {
        super(parameters, leftBrace, rightBrace);
        this.operatorKeyword = operatorKeyword;
        this.position = this.operatorKeyword.getPosition().union(rightBrace.getPosition());
    }

    @Override
    public IPosition getPosition() {
        return position;
    }

    @Override
    public IPosition getNamePosition() {
        return operatorKeyword.getPosition();
    }

    @Override
    public String getName() {
        return Name;
    }

    @Override
    public String getJavaName() {
        return getName();
    }

    @Override
    public IVariableType getVariableType() {
        return new VariableType(getCompilationUnit(), getDeclaringType());
    }

    @Override
    public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
        setVariableType(new VariableType(compilationUnit, declaringType));
        return super.resolveTypes(compilationUnit, declaringType);
    }

    @Override
    public boolean resolveStructure(CompilationUnit compilationUnit, IType declaringType) {
        if(!super.resolveStructure(compilationUnit, declaringType))
            return false;

        if(getParametersCount() != 0) {
            setError(getPosition(), "Priority operator must not have parameter(s)");
        }

        return true;
    }
}
