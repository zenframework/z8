package org.zenframework.z8.compiler.parser.grammar.lexer.token;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.parser.type.Primary;
import org.zenframework.z8.compiler.util.Datespan;

public class DatespanToken extends ConstantToken {
	private Datespan value;

	public DatespanToken() {
	}

	public DatespanToken(Datespan value, IPosition position) {
		super(position);
		this.value = value;
	}

	public Datespan getValue() {
		return value;
	}

	@Override
	public String format(boolean forCodeGeneration) {
		return '"' + value.toString() + '"';
	}

	@Override
	public String getTypeName() {
		return Primary.Datespan;
	}

	@Override
	public String getSqlTypeName() {
		return Primary.SqlDatespan;
	}
}