package org.zenframework.z8.compiler.parser.expressions;

import org.zenframework.z8.compiler.content.LabelEntry;
import org.zenframework.z8.compiler.content.LabelProvider;
import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.ConstantToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.StringToken;
import org.zenframework.z8.compiler.parser.type.Primary;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class Constant extends LanguageElement {
    private ConstantToken token;
    private IType type;

    public Constant(ConstantToken token) {
        this.token = token;
    }

    @Override
    public IPosition getSourceRange() {
        return token.getPosition();
    }

    @Override
    public IToken getFirstToken() {
        return getToken();
    }

    public ConstantToken getToken() {
        return token;
    }

    @Override
    public IVariableType getVariableType() {
        if(type != null) {
            return new VariableType(getCompilationUnit(), type);
        }
        return null;
    }

    @Override
    public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
        if(!super.resolveTypes(compilationUnit, declaringType))
            return false;

        type = Primary.resolveType(compilationUnit, token.getTypeName());

        return type != null;
    }

    public boolean isNLSString() {
        if(token instanceof StringToken) {
            String value = token.getValueString();
            return value.length() > 2 && value.charAt(0) == '$' && value.charAt(value.length() - 1) == '$';
        }
        return false;
    }

    @Override
    public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod,
            IVariable leftHandValue, IVariableType context) {
        if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null) || type == null)
            return false;

        if(isNLSString()) {
            ConstantToken token = getToken();
            String value = token.getValueString();
            String key = value.substring(1, value.length() - 1);

            getCompilationUnit().addNLSString(token);

            LabelEntry entry = LabelProvider.getEntry(compilationUnit.getProject(), "ru", key);

            if(entry == null) {
                getCompilationUnit().addNLSString(token);
                setError(getToken().getPosition(), "The resource string " + value + " is not found.");
                return false;
            }

            entry.getNLSUnit().addConsumer(compilationUnit);
        }

        return true;
    }

    @Override
    public void getCode(CodeGenerator codeGenerator) {
        codeGenerator.getCompilationUnit().importType(type);
        codeGenerator.append("new " + getVariableType().getJavaName() + "(" + token.format(true) + ")");
    }
}
