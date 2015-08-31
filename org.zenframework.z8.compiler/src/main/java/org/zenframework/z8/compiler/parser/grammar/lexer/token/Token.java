package org.zenframework.z8.compiler.parser.grammar.lexer.token;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;

public class Token implements IToken {
    private int id;
    private String rawText;
    private IPosition position;

    public Token() {
        this.id = IToken.NOTHING;
    }

    public Token(int id, IPosition position) {
        this(id, position, null);
    }

    public Token(int id, IPosition position, String rawText) {
        this.id = id;
        this.position = position;
        this.rawText = rawText;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public IPosition getPosition() {
        return position;
    }

    @Override
    public String getRawText() {
        return rawText;
    }
}
