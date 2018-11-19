package org.zenframework.z8.compiler.parser.grammar.lexer.token;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.parser.type.Primary;

public class IntegerToken extends ConstantToken {
	private long value;

	public IntegerToken() {
	}

	public IntegerToken(long value, IPosition position) {
		super(position);
		this.value = value;
	}

	public long getValue() {
		return value;
	}

	@Override
	public String format(boolean forCodeGeneration) {
		return Long.toString(value) + (forCodeGeneration ? "L" : "");
	}

	@Override
	public String getTypeName() {
		return Primary.Integer;
	}

	@Override
	public String getSqlTypeName() {
		return Primary.SqlInteger;
	}
}
