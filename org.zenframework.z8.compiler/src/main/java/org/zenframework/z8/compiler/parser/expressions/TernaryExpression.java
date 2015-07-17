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
import org.zenframework.z8.compiler.parser.type.Primary;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class TernaryExpression extends LanguageElement {
    private ILanguageElement condition;
    private ILanguageElement left;
    private ILanguageElement right;

    private IVariableType variableType;

    private ITypeCast conditionTypeCast;
    private ITypeCast leftToRightTypeCast;
    private ITypeCast rightToLeftTypeCast;

    public TernaryExpression(ILanguageElement condition, ILanguageElement left, ILanguageElement right) {
        this.condition = condition;
        this.left = left;
        this.right = right;
    }

    @Override
    public IPosition getSourceRange() {
        return condition.getSourceRange().union(right.getSourceRange());
    }

    @Override
    public IToken getFirstToken() {
        return condition.getFirstToken();
    }

    @Override
    public IVariableType getVariableType() {
        return variableType;
    }

    @Override
    public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
        if(!super.resolveTypes(compilationUnit, declaringType))
            return false;

        boolean result = condition.resolveTypes(compilationUnit, declaringType);
        result &= left.resolveTypes(compilationUnit, declaringType);
        result &= right.resolveTypes(compilationUnit, declaringType);

        return result;
    }

    @Override
    public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod,
            IVariable leftHandValue, IVariableType context) {
        if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
            return false;

        boolean result = condition.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
        result &= left.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
        result &= right.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);

        if(!result)
            return false;

        IType booleanType = Primary.resolveType(compilationUnit, Primary.Boolean);

        if(booleanType != null) {
            conditionTypeCast = condition.getVariableType().getCastTo(booleanType);

            if(conditionTypeCast == null) {
                setError(condition.getPosition(), "Type mismatch: cannot convert from "
                        + condition.getVariableType().getSignature() + " to " + booleanType.getUserName());
                return false;
            }
        }

        leftToRightTypeCast = left.getVariableType().getCastTo(right.getVariableType());

        if(leftToRightTypeCast != null) {
            variableType = right.getVariableType();
            return true;
        }

        rightToLeftTypeCast = right.getVariableType().getCastTo(left.getVariableType());

        if(rightToLeftTypeCast != null) {
            variableType = left.getVariableType();
            return true;
        }

        IPosition position = left.getPosition().union(right.getPosition());
        setError(position, "Operator ? : types " + left.getVariableType().getSignature() + " and "
                + right.getVariableType().getSignature() + " are not compatible");

        return false;
    }

    @Override
    public void getCode(CodeGenerator codeGenerator) {
        conditionTypeCast.getCode(codeGenerator, condition);
        codeGenerator.append(".get() ? ");

        if(leftToRightTypeCast != null) {
            leftToRightTypeCast.getCode(codeGenerator, left);
        }
        else {
            left.getCode(codeGenerator);
        }

        codeGenerator.append(" : ");

        if(rightToLeftTypeCast != null) {
            rightToLeftTypeCast.getCode(codeGenerator, right);
        }
        else {
            right.getCode(codeGenerator);
        }
    }
}
