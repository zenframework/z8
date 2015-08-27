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
import org.zenframework.z8.compiler.parser.type.TypeCast;
import org.zenframework.z8.compiler.parser.variable.Variable;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class MapElement extends LanguageElement {
    private IToken leftBrace;
    private IToken rightBrace;

    private ILanguageElement key;
    private ILanguageElement value;

    private IVariableType variableType;

    private ITypeCast keyTypeCast;
    private ITypeCast valueTypeCast;

    public MapElement(IToken leftBrace, ILanguageElement key, ILanguageElement value, IToken rightBrace) {
        this.leftBrace = leftBrace;
        this.rightBrace = rightBrace;
        this.key = key;
        this.value = value;
    }

    public ILanguageElement getKey() {
        return key;
    }

    public ILanguageElement getValue() {
        return value;
    }

    @Override
    public IPosition getSourceRange() {
        return leftBrace.getPosition().union(rightBrace.getPosition());
    }

    @Override
    public IToken getFirstToken() {
        return leftBrace;
    }

    @Override
    public IVariableType getVariableType() {
        return variableType;
    }

    @Override
    public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
        if(!super.resolveTypes(compilationUnit, declaringType))
            return false;

        return key.resolveTypes(compilationUnit, declaringType) && value.resolveTypes(compilationUnit, declaringType);
    }

    @Override
    public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod,
            IVariable leftHandValue, IVariableType context) {
        if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
            return false;

        IVariableType leftType = leftHandValue.getVariableType();

        if(!leftType.isArray() || leftType.getRightKey() == null) {
            setError(getPosition(), "The type " + leftHandValue.getSignature() + " is not a map");
            return false;
        }

        // left - "int[][string]", value  - int[], key - string

        IVariableType valueType = new VariableType(leftType);
        valueType.removeRightKey();

        if(!key.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null)
                || !value.checkSemantics(compilationUnit, declaringType, declaringMethod, new Variable(valueType), null))
            return false;

        if(key.getVariableType().isArray()) {
            setError(key.getPosition(), "Array (map) type " + key.getVariableType().getSignature()
                    + " cannot be used as a map key type");
            return false;
        }

        IVariableType keyVariableType = key.getVariableType();
        IVariableType valueVariableType = value.getVariableType();

        keyTypeCast = new TypeCast(keyVariableType, keyVariableType, 0);
        valueTypeCast = new TypeCast(valueVariableType, valueVariableType, 0);

        variableType = new VariableType(value.getVariableType());
        variableType.addRightKey(key.getVariableType().getType());

        return true;
    }

    @Override
    public void getCode(CodeGenerator codeGenerator) {
        keyTypeCast.getCode(codeGenerator, key, true);
        codeGenerator.append(", ");
        valueTypeCast.getCode(codeGenerator, value, true);
    }

    public void getKeyCode(CodeGenerator codeGenerator) {
        keyTypeCast.getCode(codeGenerator, key, true);
    }

    public void getValueCode(CodeGenerator codeGenerator) {
        valueTypeCast.getCode(codeGenerator, value, true);
    }
}
