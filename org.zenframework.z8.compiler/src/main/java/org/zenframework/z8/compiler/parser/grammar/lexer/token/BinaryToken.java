package org.zenframework.z8.compiler.parser.grammar.lexer.token;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.parser.type.Primary;
import org.zenframework.z8.compiler.util.Binary;

public class BinaryToken extends ConstantToken {
    private Binary value;

    public BinaryToken() {}

    public BinaryToken(Binary value, IPosition position) {
        super(position);
        this.value = value;
    }

    public Binary getValue() {
        return value;
    }

    @Override
    public String format(boolean forCodeGeneration) {
        return forCodeGeneration ? value.toString() : value.toShortString();
    }

    @Override
    public String getTypeName() {
        return Primary.Binary;
    }

    @Override
    public String getSqlTypeName() {
        return Primary.SqlBinary;
    }
}
