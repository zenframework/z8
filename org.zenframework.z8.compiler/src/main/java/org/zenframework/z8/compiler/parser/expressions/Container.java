package org.zenframework.z8.compiler.parser.expressions;

import java.util.ArrayList;
import java.util.List;

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

public class Container extends LanguageElement {
    private List<IToken> tokens = new ArrayList<IToken>();
    private IVariableType variableType;

    public Container(IToken container) {
        tokens.add(container);
    }

    public void add(IToken container) {
        tokens.add(container);
    }

    @Override
    public IPosition getSourceRange() {
        return tokens.get(0).getPosition().union(tokens.get(tokens.size() - 1).getPosition());
    }

    @Override
    public IToken getFirstToken() {
        return tokens.get(0);
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
    public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
        return super.resolveTypes(compilationUnit, declaringType);
    }

    @Override
    public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod,
            IVariable leftHandValue, IVariableType context) {
        if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
            return false;

        IType type = getDeclaringType();

        for(int i = 0; i < tokens.size(); i++) {
            IType container = type.getContainerType();

            if(container == null) {
                setError(tokens.get(i).getPosition(), "container keyword can not be used in this context");
                return false;
            }

            variableType = new VariableType(getCompilationUnit(), container);

            compilationUnit.addHyperlink(tokens.get(i).getPosition(), container);
            compilationUnit.addContentProposal(tokens.get(i).getPosition(), variableType);

            type = container;
        }

        if(getStaticContext()) {
            setError(getPosition(), "cannot use container in a static context");
        }

        return true;
    }

    @Override
    public void getCode(CodeGenerator codeGenerator) {
        String code = "";

        for(int i = 0; i < tokens.size(); i++) {
            code += (i == 0 ? "" : ".") + "getContainer()";
        }

        IType type = getVariableType().getType();
        String containerType = type.getNestedJavaName();

        IType topLevelContainer = type.getCompilationUnit().getType();

        codeGenerator.getCompilationUnit().importType(topLevelContainer);

        if(!variableType.extendsPrimary()) {
            codeGenerator.append("((" + containerType + ".CLASS<" + containerType + ">)" + code + ".getCLASS())");
        }
        else {
            codeGenerator.append("((" + containerType + ")" + code + ")");
        }
    }

    public int getNumber() {
        return tokens.size();
    }
}
