package org.zenframework.z8.server.expression;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;

public enum Operator {

	PLUS("+"),
	MINUS("-"),
	TIMES("*"),
	DIV("/"),
	GT(">"),
	LT("<"),
	GE(">="),
	LE("<="),
	EQ("=="),
	NE("!="),
	NOT("!"),
	AND("&"),
	OR("|"),
	XOR("^");

	private final String operator;

	private Operator(String operator) {
		this.operator = operator;
	}

	@Override
	public String toString() {
		return operator;
	}

	public static Operator parse(Token token) {
		return token != null ? parse(token.getText()) : null;
	}

	public static Operator parse(RuleContext ctx) {
		return ctx != null ? parse(ctx.getText()) : null;
	}

	public static Operator parse(String operator) {
		for (Operator op : values())
			if (op.operator.equals(operator))
				return op;
		return null;
	}
}
