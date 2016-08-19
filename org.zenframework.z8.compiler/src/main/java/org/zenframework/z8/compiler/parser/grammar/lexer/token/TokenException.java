package org.zenframework.z8.compiler.parser.grammar.lexer.token;

import org.zenframework.z8.compiler.core.IPosition;

public class TokenException extends RuntimeException {
	private static final long serialVersionUID = 3257565101040022069L;

	private IPosition position;

	public TokenException(String message, IPosition position) {
		super(message);
		this.position = position;
	}

	public TokenException(IPosition position) {
		super();
		this.position = position;
	}

	public IPosition getPosition() {
		return position;
	}
}
