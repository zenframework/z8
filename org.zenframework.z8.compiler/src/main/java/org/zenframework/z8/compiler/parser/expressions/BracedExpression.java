package org.zenframework.z8.compiler.parser.expressions;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.type.members.PriorityOperator;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class BracedExpression extends LanguageElement {
    private IToken leftBrace;
    private IToken rightBrace;
    private ILanguageElement expression;

    public BracedExpression(IToken leftBrace, ILanguageElement expression, IToken rightBrace) {
        this.leftBrace = leftBrace;
        this.expression = expression;
        this.rightBrace = rightBrace;
    }

    @Override
    public IPosition getSourceRange() {
        return leftBrace.getPosition().union(expression.getSourceRange());
    }

    @Override
    public IPosition getPosition() {
        return expression.getPosition();
    }

    @Override
    public IToken getFirstToken() {
        return leftBrace;
    }

    @Override
    public boolean isQualifiedName() {
        return expression.isQualifiedName();
    }

    @Override
    public boolean isOperatorNew() {
        return expression.isOperatorNew();
    }

    public ILanguageElement getExpression() {
        return expression;
    }

    public TypeCastExpression toTypeCastExpression() {
        if(expression instanceof Postfix) {
            Postfix postfix = (Postfix)expression;

            if(postfix.getPostfix() != null) {
                return null;
            }

            ILanguageElement prefix = postfix.getPrefix();

            if(prefix instanceof QualifiedName) {
                QualifiedName qualifiedName = (QualifiedName)prefix;
                IToken[] tokens = qualifiedName.getTokens();

                if(tokens.length == 1) {
                    return new TypeCastExpression(leftBrace, tokens[0], rightBrace);
                }
            }
        }

        return null;
    }

    @Override
    public IVariable getVariable() {
        return expression.getVariable();
    }

    @Override
    public IVariableType getVariableType() {
        return expression.getVariableType();
    }

    @Override
    public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
        return super.resolveTypes(compilationUnit, declaringType)
                && expression.resolveTypes(compilationUnit, declaringType);
    }

    @Override
    public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod,
            IVariable leftHandValue, IVariableType context) {
        return super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null)
                && expression.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
    }

    @Override
    public void getCode(CodeGenerator codeGenerator) {
        IVariableType variableType = getVariableType();

        IMethod[] methods = variableType.getMatchingMethods(PriorityOperator.Name);

        if(methods.length == 1) {
            expression.getCode(codeGenerator);

            if(variableType.isReference()) {
                codeGenerator.append(".get()");
            }

            codeGenerator.append("." + methods[0].getJavaName() + "()");
        }
        else if(expression instanceof TernaryExpression) {
            codeGenerator.append('(');
            expression.getCode(codeGenerator);
            codeGenerator.append(')');
        }
        else {
            expression.getCode(codeGenerator);
        }
    }
}
