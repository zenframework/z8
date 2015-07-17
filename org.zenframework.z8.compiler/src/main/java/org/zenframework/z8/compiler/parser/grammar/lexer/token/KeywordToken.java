package org.zenframework.z8.compiler.parser.grammar.lexer.token;

import org.zenframework.z8.compiler.core.IPosition;

public class KeywordToken extends Token {
    static public final String Operator = "operator";

    public KeywordToken() {}

    public KeywordToken(int id, IPosition position) {
        super(id, position);
    }
}
