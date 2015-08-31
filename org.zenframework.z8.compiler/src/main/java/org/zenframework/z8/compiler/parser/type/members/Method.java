package org.zenframework.z8.compiler.parser.type.members;

import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.parser.variable.Variable;
import org.zenframework.z8.compiler.parser.variable.VariableType;

public class Method extends AbstractMethod implements IMethod {
    private IToken nameToken;
    private String name;

    public Method(VariableType type, IToken nameToken, Variable[] parameters, IToken leftBrace, IToken rightBrace) {
        super(type, parameters, leftBrace, rightBrace);

        this.nameToken = nameToken;
        this.name = this.nameToken.getRawText();
    }

    public Method(VariableType type, String name, Variable[] parameters) {
        super(type, parameters, null, null);
        this.name = name;
    }

    @Override
    public IPosition getNamePosition() {
        if(nameToken == null) {
            return null;
        }
        return nameToken.getPosition();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getJavaName() {
        return "z8_" + getName();
    }
}
