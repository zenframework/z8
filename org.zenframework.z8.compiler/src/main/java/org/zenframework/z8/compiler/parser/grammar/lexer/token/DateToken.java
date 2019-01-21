package org.zenframework.z8.compiler.parser.grammar.lexer.token;

import java.util.regex.Pattern;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.parser.type.Primary;
import org.zenframework.z8.compiler.util.Date;

public class DateToken extends ConstantToken {
	private static final String CRON_COLUMN = "((\\*)|(\\d{1,2}(-\\d{1,2})?))(/\\d{1,2})?(\\,((\\*)|(\\d{1,2}(-\\d{1,2})?))(/\\d{1,2})?)*";
	private static final Pattern CRON_PATTERN = Pattern
			.compile("^" + CRON_COLUMN + "\\s+" + CRON_COLUMN + "\\s+" + CRON_COLUMN + "\\s+" + CRON_COLUMN + "\\s+" + CRON_COLUMN + "$");

	private Date value;

	public DateToken() {
	}

	public DateToken(Date value, IPosition position) {
		super(position);
		this.value = value;
	}

	public Date getValue() {
		return value;
	}

	@Override
	public String format(boolean forCodeGeneration) {
		return forCodeGeneration ? Long.toString(value.getTicks()) + "L" : value.toString();
	}

	@Override
	public String getTypeName() {
		return Primary.Date;
	}

	@Override
	public String getSqlTypeName() {
		return Primary.SqlDate;
	}

	public static boolean checkCron(String cronExp) {
		return CRON_PATTERN.matcher(cronExp).matches();
	}


}