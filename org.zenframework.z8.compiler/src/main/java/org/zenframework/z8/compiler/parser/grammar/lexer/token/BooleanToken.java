package org.zenframework.z8.compiler.parser.grammar.lexer.token;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.parser.type.Primary;

public class BooleanToken extends ConstantToken {
    private boolean value;

    public BooleanToken() {}

    public BooleanToken(boolean value, IPosition position) {
        super(position);
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public String format(boolean forCodeGeneration) {
        return value ? "true" : "false";
    }

    @Override
    public String getTypeName() {
        return Primary.Boolean;
    }

    @Override
    public String getSqlTypeName() {
        return Primary.SqlBoolean;
    }
}
