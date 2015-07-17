package org.zenframework.z8.compiler.parser.expressions;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class This extends LanguageElement {
    private IToken thisToken;
    private IVariableType variableType;

    public This(IToken token) {
        thisToken = token;
    }

    @Override
    public IPosition getSourceRange() {
        return thisToken.getPosition();
    }

    @Override
    public IToken getFirstToken() {
        return thisToken;
    }

    @Override
    public IVariableType getVariableType() {
        return variableType;
    }

    @Override
    public boolean isQualifiedName() {
        return true;
    }

    @Override
    public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod,
            IVariable leftHandValue, IVariableType context) {
        if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
            return false;

        variableType = new VariableType(getCompilationUnit(), declaringType);

        compilationUnit.addHyperlink(thisToken.getPosition(), declaringType);
        compilationUnit.addContentProposal(thisToken.getPosition(), variableType);

        if(getStaticContext()) {
            setError(getPosition(), "cannot use this in a static context");
        }

        return true;
    }

    @Override
    public void getCode(CodeGenerator codeGenerator) {
        if(getDeclaringType().extendsPrimary()) {
            codeGenerator.append("this");
        }
        else {
            String type = getVariableType().getType().getNestedJavaName();
            codeGenerator.append("((" + type + ".CLASS<" + type + ">)getCLASS())");
        }
    }
}
