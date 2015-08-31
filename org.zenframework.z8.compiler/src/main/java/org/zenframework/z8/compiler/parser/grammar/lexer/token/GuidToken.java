package org.zenframework.z8.compiler.parser.grammar.lexer.token;

import java.util.UUID;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.parser.type.Primary;

public class GuidToken extends ConstantToken {
    private UUID value;

    public GuidToken() {}

    public GuidToken(UUID value, IPosition position) {
        super(position);
        this.value = value;
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public String format(boolean forCodeGeneration) {
        return forCodeGeneration ? '"' + value.toString() + '"' : value.toString();
    }

    @Override
    public String getTypeName() {
        return Primary.Guid;
    }

    @Override
    public String getSqlTypeName() {
        return Primary.SqlGuid;
    }
}