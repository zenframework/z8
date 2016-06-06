package org.zenframework.z8.server.types;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.exceptions.MaskParseException;
import org.zenframework.z8.server.types.sql.sql_decimal;

public final class decimal extends primary {

	private static final long serialVersionUID = 2752764281506888344L;

	static public integer ROUND_UP = new integer(BigDecimal.ROUND_UP);
	static public integer ROUND_DOWN = new integer(BigDecimal.ROUND_DOWN);
	static public integer ROUND_CEILING = new integer(BigDecimal.ROUND_CEILING);
	static public integer ROUND_FLOOR = new integer(BigDecimal.ROUND_FLOOR);
	static public integer ROUND_HALF_UP = new integer(BigDecimal.ROUND_HALF_UP);
	static public integer ROUND_HALF_DOWN = new integer(BigDecimal.ROUND_HALF_DOWN);
	static public integer ROUND_HALF_EVEN = new integer(BigDecimal.ROUND_HALF_EVEN);
	static public integer ROUND_UNNECESSARY = new integer(BigDecimal.ROUND_UNNECESSARY);

	private BigDecimal m_value = new BigDecimal(0);

	private static int maxPrecision = 38;

	public decimal() {}

	public decimal(String string) {
		set(string);
	}

	public decimal(string string) {
		this(string.get());
	}

	public decimal(decimal decimal) {
		set(decimal);
	}

	public decimal(integer x) {
		set(x);
	}

	public decimal(double decimal) {
		set(BigDecimal.valueOf(decimal));
	}

	public decimal(BigDecimal decimal) {
		set(decimal);
	}

	public decimal(long x) {
		set(new BigDecimal(x));
	}

	@Override
	public decimal defaultValue() {
		return new decimal();
	}

	@Override
	public String toString() {
		return m_value.toPlainString();
	}

	public BigDecimal get() {
		return m_value;
	}

	public double getDouble() {
		return m_value.doubleValue();
	}

	public void set(BigDecimal value) {
		m_value = new BigDecimal(value.unscaledValue(), value.scale());
		if (value.precision() > decimal.maxPrecision) {
			m_value = m_value.setScale(value.scale() - (value.precision() - maxPrecision), RoundingMode.CEILING);
		}
	}

	public void set(int value) {
		set(new BigDecimal(value));
	}

	public void set(double value) {
		set(new BigDecimal(value));
	}

	public void set(integer value) {
		set(new BigDecimal(value.get()));
	}

	public void set(decimal value) {
		set(value.get());
	}

	public void set(String strConst) {
		set(z8_parse(new string(strConst)));
	}

	@Override
	public FieldType type() {
		return FieldType.Decimal;
	}

	@Override
	public String toDbConstant(DatabaseVendor dbtype) {
		switch (dbtype) {
		case SqlServer: {
			int scale = (scale() == 0 ? 1 : scale());
			int precision = ((precision() + scale) > maxPrecision ? maxPrecision - scale : precision()) + scale;
			return "CONVERT([numeric](" + java.lang.Integer.toString(precision) + "," + java.lang.Integer.toString(scale)
					+ "),(" + toString() + "),(0))";
		}
		default:
			return toString();
		}
	}

	@Override
	public int hashCode() {
		return m_value.hashCode();
	}

	@Override
	public boolean equals(Object d) {
		if (d instanceof decimal) {
			return m_value.compareTo(((decimal) d).m_value) == 0;
		}
		return false;
	}

	public int precision() {
		return m_value.precision();
	}

	public int scale() {
		return m_value.scale();
	}

	public sql_decimal sql_decimal() {
		return new sql_decimal(this);
	}

	public void operatorAssign(integer value) {
		set(value);
	}

	public void operatorAssign(decimal value) {
		set(value);
	}

	public decimal operatorAdd() {
		return this;
	}

	public decimal operatorSub() {
		return new decimal(m_value.negate());
	}

	public decimal operatorAdd(integer x) {
		return new decimal(m_value.add(new BigDecimal(x.get())));
	}

	public decimal operatorSub(integer x) {
		return new decimal(m_value.subtract(new BigDecimal(x.get())));
	}

	public decimal operatorMul(integer x) {
		return new decimal(m_value.multiply(new BigDecimal(x.get())));
	}

	public decimal operatorMod(integer x) {
		try {
			return new decimal(m_value.divideAndRemainder(new BigDecimal(x.get()))[1]);
		} catch (ArithmeticException e) {
			throw new RuntimeException(e);
		}
	}

	public decimal operatorAdd(decimal x) {
		return new decimal(m_value.add(x.m_value));
	}

	public decimal operatorSub(decimal x) {
		return new decimal(m_value.subtract(x.m_value));
	}

	public decimal operatorMul(decimal x) {
		return new decimal(m_value.multiply(x.m_value));
	}

	public decimal operatorDiv(integer x) {
		return operatorDiv(new decimal(x));
	}

	public decimal operatorDiv(decimal x) {
		BigDecimal divisor = x.get();

		MathContext mc = new MathContext((int) Math.min(precision() + (long) Math.ceil(10.0 * divisor.precision() / 3.0),
				java.lang.Integer.MAX_VALUE));

		try {
			return new decimal(m_value.divide(divisor, mc));
		} catch (ArithmeticException e) {
			throw new RuntimeException(e);
		}
	}

	public decimal operatorMod(decimal x) {
		try {
			return new decimal(m_value.divideAndRemainder(x.get())[1]);
		} catch (ArithmeticException e) {
			throw new RuntimeException(e);
		}
	}

	public decimal operatorAddAssign(integer x) {
		set(operatorAdd(x));
		return this;
	}

	public decimal operatorSubAssign(integer x) {
		set(operatorSub(x));
		return this;
	}

	public decimal operatorMulAssign(integer x) {
		set(operatorMul(x));
		return this;
	}

	public decimal operatorDivAssign(integer x) {
		set(operatorDiv(x));
		return this;
	}

	public decimal operatorModAssign(integer x) {
		set(operatorMod(x));
		return this;
	}

	public decimal operatorAddAssign(decimal x) {
		set(operatorAdd(x));
		return this;
	}

	public decimal operatorSubAssign(decimal x) {
		set(operatorSub(x));
		return this;
	}

	public decimal operatorMulAssign(decimal x) {
		set(operatorMul(x));
		return this;
	}

	public decimal operatorDivAssign(decimal x) {
		set(operatorDiv(x));
		return this;
	}

	public decimal operatorModAssign(decimal x) {
		set(operatorMod(x));
		return this;
	}

	public bool operatorEqu(integer x) {
		return operatorEqu(new decimal(x));
	}

	public bool operatorNotEqu(integer x) {
		return operatorNotEqu(new decimal(x));
	}

	public bool operatorLess(integer x) {
		return operatorLess(new decimal(x));
	}

	public bool operatorMore(integer x) {
		return operatorMore(new decimal(x));
	}

	public bool operatorLessEqu(integer x) {
		return operatorLessEqu(new decimal(x));
	}

	public bool operatorMoreEqu(integer x) {
		return operatorMoreEqu(new decimal(x));
	}

	public bool operatorEqu(decimal x) {
		return new bool(m_value.compareTo(x.get()) == 0);
	}

	public bool operatorNotEqu(decimal x) {
		return new bool(m_value.compareTo(x.get()) != 0);
	}

	public bool operatorLess(decimal x) {
		return new bool(m_value.compareTo(x.get()) < 0);
	}

	public bool operatorMore(decimal x) {
		return new bool(m_value.compareTo(x.get()) > 0);
	}

	public bool operatorLessEqu(decimal x) {
		return new bool(m_value.compareTo(x.get()) <= 0);
	}

	public bool operatorMoreEqu(decimal x) {
		return new bool(m_value.compareTo(x.get()) >= 0);
	}

	public decimal z8_abs() {
		return new decimal(Math.abs(m_value.doubleValue()));
	}

	public integer z8_signum() {
		return new integer((long) Math.signum(m_value.doubleValue()));
	}

	public integer z8_ceil() {
		return new integer((long) Math.ceil(m_value.doubleValue()));
	}

	public integer z8_floor() {
		return new integer((long) Math.floor(m_value.doubleValue()));
	}

	public integer round() {
		return new integer(Math.round(m_value.doubleValue()));
	}

	public decimal round(int digits) {
		return round(digits, ROUND_HALF_UP);
	}

	public decimal round(int digits, integer mode) {
		return new decimal(m_value.setScale(digits, RoundingMode.valueOf(mode.getInt())));
	}

	public decimal round(integer digits) {
		return round(digits.getInt());
	}

	public decimal round(integer digits, integer mode) {
		return round(digits.getInt(), mode);
	}

	public integer z8_round() {
		return round();
	}

	public decimal z8_round(integer digits) {
		return round(digits);
	}

	public decimal z8_round(integer digits, integer mode) {
		return round(digits, mode);
	}

	public decimal z8_sin() {
		try {
			return new decimal(Math.sin(m_value.doubleValue()));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_cos() {
		try {
			return new decimal(Math.cos(m_value.doubleValue()));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_tan() {
		try {
			return new decimal(Math.tan(m_value.doubleValue()));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_asin() {
		try {
			return new decimal(Math.asin(m_value.doubleValue()));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_acos() {
		try {
			return new decimal(Math.acos(m_value.doubleValue()));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_atan() {
		try {
			return new decimal(Math.atan(m_value.doubleValue()));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_sinh() {
		try {
			return new decimal(Math.sinh(m_value.doubleValue()));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_cosh() {
		try {
			return new decimal(Math.cosh(m_value.doubleValue()));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_tanh() {
		try {
			return new decimal(Math.tanh(m_value.doubleValue()));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_toRadians() {
		try {
			return new decimal(Math.toRadians(m_value.doubleValue()));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_toDegrees() {
		try {
			return new decimal(Math.toDegrees(m_value.doubleValue()));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_exp() {
		try {
			return new decimal(Math.exp(m_value.doubleValue()));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_ln() {
		try {
			return new decimal(Math.log(m_value.doubleValue()));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_log10() {
		try {
			return new decimal(Math.log10(m_value.doubleValue()));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_sqrt() {
		try {
			return new decimal(Math.sqrt(m_value.doubleValue()));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_cbrt() {
		try {
			return new decimal(Math.cbrt(m_value.doubleValue()));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_power(decimal power) {
		return new decimal(Math.pow(m_value.doubleValue(), power.get().doubleValue()));
	}

	public decimal z8_power(integer power) {
		return new decimal(Math.pow(m_value.doubleValue(), power.get()));
	}

	static public decimal z8_parse(string string) {
		try {
			String str = string.get();
			if (str.length() == 0)
				str = "0.0";
			return new decimal(new BigDecimal(str));
		} catch (NumberFormatException e) {
			throw new MaskParseException(string, "decimal");
		}
	}

	static public decimal z8_parse(integer doubleBit) {
		return new decimal(Double.longBitsToDouble(doubleBit.get()));
	}
}
