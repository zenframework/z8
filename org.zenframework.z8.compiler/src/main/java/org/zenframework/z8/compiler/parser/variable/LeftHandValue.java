package org.zenframework.z8.compiler.parser.variable;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IAttribute;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Project;

public class LeftHandValue implements IVariable {
    private ILanguageElement element;

    public LeftHandValue(ILanguageElement element) {
        this.element = element;
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    @Override
    public IPosition getSourceRange() {
        return element.getSourceRange();
    }

    @Override
    public IPosition getPosition() {
        return element.getPosition();
    }

    @Override
    public IToken getFirstToken() {
        return element.getFirstToken();
    }

    @Override
    public IAttribute[] getAttributes() {
        return element.getAttributes();
    }

    @Override
    public IAttribute getAttribute(String name) {
        return element.getAttribute(name);
    }

    @Override
    public void setAttributes(IAttribute[] attributes) {
        element.setAttributes(attributes);
    }

    @Override
    public IVariableType getVariableType() {
        return element.getVariableType();
    }

    @Override
    public CompilationUnit getCompilationUnit() {
        return element.getCompilationUnit();
    }

    @Override
    public Project getProject() {
        return element.getProject();
    }

    @Override
    public String getName() {
        assert (false);
        return null;
    }

    @Override
    public String getJavaName() {
        CodeGenerator codeGenerator = new CodeGenerator(getCompilationUnit());
        element.getCode(codeGenerator);
        return codeGenerator.toString();
    }

    @Override
    public String getUserName() {
        return getName() + " " + getSignature();
    }

    @Override
    public String getSignature() {
        return getVariableType().getSignature();
    }
}
