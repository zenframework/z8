package org.zenframework.z8.compiler.parser.statements;

import org.eclipse.text.edits.TextEdit;

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
import org.zenframework.z8.compiler.parser.type.Primary;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class LoopCondition extends LanguageElement {
    private ILanguageElement expression;

    private ITypeCast typeCast;

    public LoopCondition(ILanguageElement expression) {
        this.expression = expression;
    }

    @Override
    public IPosition getSourceRange() {
        return expression.getSourceRange();
    }

    @Override
    public IToken getFirstToken() {
        return expression.getFirstToken();
    }

    public void setExpression(ILanguageElement expression) {
        this.expression = expression;
    }

    @Override
    public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
        if(!super.resolveTypes(compilationUnit, declaringType))
            return false;

        if(expression != null && !expression.resolveTypes(compilationUnit, declaringType))
            return false;

        return true;
    }

    @Override
    public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod,
            IVariable leftHandValue, IVariableType context) {
        if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
            return false;

        IType booleanType = Primary.resolveType(compilationUnit, Primary.Boolean);

        if(expression != null && booleanType != null
                && expression.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null)) {
            typeCast = expression.getVariableType().getCastTo(booleanType);

            if(typeCast == null) {
                setError(expression.getPosition(), "Type mismatch: cannot convert from "
                        + expression.getVariableType().getSignature() + " to " + booleanType.getUserName());
                return false;
            }
        }

        return true;
    }

    @Override
    public void getCode(CodeGenerator codeGenerator) {
        typeCast.getCode(codeGenerator, expression);
        codeGenerator.append(".get()");
    }

    @Override
    public void replaceTypeName(TextEdit parent, IType type, String newTypeName) {
        expression.replaceTypeName(parent, type, newTypeName);
    }

}
