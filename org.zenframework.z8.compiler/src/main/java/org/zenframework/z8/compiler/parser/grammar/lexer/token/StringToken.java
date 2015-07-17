package org.zenframework.z8.compiler.parser.grammar.lexer.token;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.parser.type.Primary;

public class StringToken extends ConstantToken {
    private String value;

    public StringToken() {}

    public StringToken(String value, IPosition position) {
        super(position);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String getValueString() {
        return getValue();
    }

    @Override
    public String format(boolean forCodeGeneration) {
        StringBuffer result = new StringBuffer();

        for(int i = 0; i < value.length(); i++) {
            char chr = value.charAt(i);

            if(chr < ' ') {
                switch(chr) {
                case 0:
                    result.append("\\0");
                    break;
                case '\b':
                    result.append("\\b");
                    break;
                case '\f':
                    result.append("\\f");
                    break;
                case '\n':
                    result.append("\\n");
                    break;
                case '\r':
                    result.append("\\r");
                    break;
                case '\t':
                    result.append("\\t");
                    break;
                case '\"':
                    result.append("\\\"");
                    break;
                case '\\':
                    result.append("\\\\");
                    break;
                case '\'':
                    result.append("\\\'");
                    break;
                default:
                    result.append("\\u00");
                    if(chr < 16)
                        result.append('0');
                    result.append(Integer.toHexString(chr));
                }
            }
            else
                switch(chr) {
                case '\"':
                    result.append("\\\"");
                    break;
                case '\\':
                    result.append("\\\\");
                    break;
                case '\'':
                    result.append("\\\'");
                    break;
                default:
                    result.append(chr);
                }
        }

        return '"' + result.toString() + '"';
    }

    @Override
    public String getTypeName() {
        return Primary.String;
    }

    @Override
    public String getSqlTypeName() {
        return Primary.SqlString;
    }
}
