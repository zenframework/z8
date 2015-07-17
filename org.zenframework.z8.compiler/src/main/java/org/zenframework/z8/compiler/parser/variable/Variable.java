package org.zenframework.z8.compiler.parser.variable;

import org.eclipse.text.edits.TextEdit;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class Variable extends LanguageElement implements IVariable {
    private IVariableType variableType;
    private IToken finalToken;
    private IToken nameToken;
    private String name;

    public Variable(IVariableType type) {
        this.variableType = type;
    }

    public Variable(IToken finalToken, IVariableType type, IToken nameToken) {
        this.variableType = type;
        this.finalToken = finalToken;
        this.nameToken = nameToken;
        this.name = this.nameToken.getRawText();
    }

    public Variable(IVariableType type, String name) {
        this.variableType = type;
        this.name = name;
    }

    @Override
    public IPosition getSourceRange() {
        return nameToken.getPosition();
    }

    @Override
    public IToken getFirstToken() {
        return getFirstToken(variableType.getFirstToken(), getFirstToken(super.getFirstToken(), finalToken));
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getJavaName() {
        return getName();
    }

    @Override
    public String getUserName() {
        return getName() + " " + getSignature();
    }

    @Override
    public IVariable getVariable() {
        return this;
    }

    @Override
    public boolean isFinal() {
        return finalToken != null;
    }

    @Override
    public IVariableType getVariableType() {
        return variableType;
    }

    @Override
    public String getSignature() {
        return getVariableType().getSignature();
    }

    @Override
    public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
        if(!super.resolveTypes(compilationUnit, declaringType))
            return false;

        VariableType variableType = (VariableType)getVariableType();

        if(!variableType.resolveTypes(compilationUnit, declaringType))
            return false;

        IPosition position = nameToken.getPosition();
        compilationUnit.addHyperlink(position, compilationUnit, position);
        return true;
    }

    @Override
    public boolean resolveStructure(CompilationUnit compilationUnit, IType declaringType) {
        VariableType variableType = (VariableType)getVariableType();

        return super.resolveStructure(compilationUnit, declaringType)
                && variableType.resolveStructure(compilationUnit, declaringType);
    }

    @Override
    public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod,
            IVariable leftHandValue, IVariableType context) {
        VariableType variableType = (VariableType)getVariableType();

        return super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null)
                && variableType.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
    }

    @Override
    public boolean resolveNestedTypes(CompilationUnit compilationUnit, IType declaringType) {
        VariableType variableType = (VariableType)getVariableType();

        return super.resolveNestedTypes(compilationUnit, declaringType)
                && variableType.resolveNestedTypes(compilationUnit, declaringType);
    }

    @Override
    public void getCode(CodeGenerator codeGenerator) {
        VariableType type = (VariableType)getVariableType();
        type.getCode(codeGenerator);
        codeGenerator.append(" " + name);
    }

    public void rename(TextEdit parent, IType type, String newTypeName) {
        variableType.replaceTypeName(parent, type, newTypeName);
    }
}
