package org.zenframework.z8.compiler.parser.expressions;

import org.eclipse.text.edits.TextEdit;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IJavaTypeCast;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.variable.LeftHandValue;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class Postfix extends LanguageElement implements IJavaTypeCast {
    private ILanguageElement prefix;
    private ILanguageElement postfix;

    public Postfix(ILanguageElement prefix) {
        this.prefix = prefix;
    }

    public Postfix(ILanguageElement prefix, ILanguageElement postfix) {
        this.prefix = prefix;
        this.postfix = postfix;
    }

    @Override
    public boolean isQualifiedName() {
        return postfix == null && prefix.isQualifiedName() || prefix.isQualifiedName() && postfix.isQualifiedName();
    }

    public ILanguageElement getPrefix() {
        return prefix;
    }

    public ILanguageElement getPostfix() {
        return postfix;
    }

    @Override
    public IPosition getSourceRange() {
        if(postfix != null) {
            return prefix.getPosition().union(postfix.getPosition());
        }

        return prefix.getSourceRange();
    }

    @Override
    public IToken getFirstToken() {
        return prefix.getFirstToken();
    }

    @Override
    public void setStaticContext(boolean staticContext) {
        super.setStaticContext(staticContext);
        prefix.setStaticContext(staticContext);
    }

    @Override
    public void setCastPending(boolean castPending) {
        if(postfix == null && prefix instanceof IJavaTypeCast) {
            IJavaTypeCast javaTypeCast = (IJavaTypeCast)prefix;
            javaTypeCast.setCastPending(castPending);
        }
    }

    @Override
    public IVariable getVariable() {
        IVariable variable = postfix != null ? postfix.getVariable() : null;

        if(variable != null) {
            return variable instanceof LeftHandValue ? new LeftHandValue(this) : super.getVariable();
        }

        return prefix.getVariable();
    }

    @Override
    public IVariableType getVariableType() {
        if(postfix != null) {
            return postfix.getVariableType();
        }

        return prefix.getVariableType();
    }

    @Override
    public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
        return super.resolveTypes(compilationUnit, declaringType) && prefix.resolveTypes(compilationUnit, declaringType)
                && (postfix == null || postfix.resolveTypes(compilationUnit, declaringType));
    }

    @Override
    public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod,
            IVariable leftHandValue, IVariableType context) {
        if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null)) {
            return false;
        }

        if(!prefix.checkSemantics(compilationUnit, declaringType, declaringMethod, null, context)) {
            return false;
        }

        if(postfix != null) {
            return postfix.checkSemantics(compilationUnit, declaringType, declaringMethod, null,
                    prefix.getVariableType());
        }
        else {
            IVariableType prefixType = prefix.getVariableType();

            if(prefixType.isStatic()) {
                setFatalError(getPosition(), prefixType.getSignature() + " cannot be resolved");
                return false;
            }
        }

        return true;
    }

    @Override
    public void getCode(CodeGenerator codeGenerator) {
        IVariableType prefixType = prefix.getVariableType();

        prefix.getCode(codeGenerator);

        if(postfix != null) {
            boolean needGet = !(prefix instanceof Super) && !prefixType.isStatic() && prefixType.isReference();

            codeGenerator.append('.');

            if(needGet) {
                codeGenerator.append("get(" + getDeclaringType().getConstructionStage() + ").");
            }

            postfix.getCode(codeGenerator);
        }
    }

    @Override
    public void replaceTypeName(TextEdit parent, IType type, String newTypeName) {
        prefix.replaceTypeName(parent, type, newTypeName);

        if(postfix != null) {
            postfix.replaceTypeName(parent, type, newTypeName);
        }
    }
}
