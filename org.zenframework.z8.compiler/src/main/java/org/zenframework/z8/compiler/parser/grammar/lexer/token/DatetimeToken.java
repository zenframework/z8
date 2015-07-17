package org.zenframework.z8.compiler.parser.grammar.lexer.token;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.parser.type.Primary;
import org.zenframework.z8.compiler.util.Datetime;

public class DatetimeToken extends ConstantToken {
    private Datetime value;

    public DatetimeToken() {}

    public DatetimeToken(Datetime value, IPosition position) {
        super(position);
        this.value = value;
    }

    public Datetime getValue() {
        return value;
    }

    @Override
    public String format(boolean forCodeGeneration) {
        return '"' + value.toString() + '"';
    }

    @Override
    public String getTypeName() {
        return Primary.Datetime;
    }

    @Override
    public String getSqlTypeName() {
        return Primary.SqlDatetime;
    }
}