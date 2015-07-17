package org.zenframework.z8.compiler.parser.expressions;

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
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class TypeCastExpression extends LanguageElement {
    private IToken leftBrace;
    private IToken rightBrace;
    private VariableType variableType;
    private ILanguageElement expression;

    private ITypeCast typeCast;

    public TypeCastExpression(IToken leftBrace, IToken typeNameToken, IToken rightBrace) {
        this.leftBrace = leftBrace;
        this.variableType = new VariableType(typeNameToken);
        this.rightBrace = rightBrace;
    }

    @Override
    public IToken getFirstToken() {
        return leftBrace;
    }

    public void setExpression(ILanguageElement expression) {
        this.expression = expression;
    }

    @Override
    public IPosition getSourceRange() {
        return leftBrace.getPosition().union(rightBrace.getPosition());
    }

    @Override
    public IVariableType getVariableType() {
        return variableType;
    }

    @Override
    public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
        return super.resolveTypes(compilationUnit, declaringType)
                && expression.resolveTypes(compilationUnit, declaringType)
                && variableType.resolveTypes(compilationUnit, declaringType);
    }

    @Override
    public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod,
            IVariable leftHandValue, IVariableType context) {
        if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null)
                || !expression.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null)
                || !variableType.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null)) {
            return false;
        }

        typeCast = expression.getVariableType().getCastTo(variableType);

        if(typeCast == null) {
            setError(getPosition(), "Type mismatch: cannot convert from " + expression.getVariableType().getSignature()
                    + " to " + getVariableType().getSignature());
            return false;
        }

        return true;
    }

    @Override
    public void getCode(CodeGenerator codeGenerator) {
        typeCast.getCode(codeGenerator, expression);
    }
}
