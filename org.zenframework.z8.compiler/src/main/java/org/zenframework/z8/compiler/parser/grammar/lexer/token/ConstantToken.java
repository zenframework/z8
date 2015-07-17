package org.zenframework.z8.compiler.parser.grammar.lexer.token;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;

abstract public class ConstantToken extends Token {
    protected ConstantToken() {}

    protected ConstantToken(IPosition position) {
        super(IToken.CONSTANT, position);
    }

    public String format() {
        return format(false);
    }

    public String format(boolean forCodeGeneration) {
        assert (false);
        return "";
    }

    public String getValueString() {
        return format(true);
    }

    abstract public String getTypeName();

    abstract public String getSqlTypeName();
}
