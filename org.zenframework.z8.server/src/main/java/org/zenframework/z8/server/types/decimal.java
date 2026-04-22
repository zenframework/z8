package org.zenframework.z8.server.types;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.types.sql.sql_decimal;

public final class decimal extends primary {
	static private final long serialVersionUID = 2752764281506888344L;

	static public integer RoundUp = new integer(BigDecimal.ROUND_UP);
	static public integer RoundDown = new integer(BigDecimal.ROUND_DOWN);
	static public integer RoundCeiling = new integer(BigDecimal.ROUND_CEILING);
	static public integer RoundFloor = new integer(BigDecimal.ROUND_FLOOR);
	static public integer RoundHalfUp = new integer(BigDecimal.ROUND_HALF_UP);
	static public integer RoundHalfDown = new integer(BigDecimal.ROUND_HALF_DOWN);
	static public integer RoundHalfEven = new integer(BigDecimal.ROUND_HALF_EVEN);
	static public integer RoundUnnecessary = new integer(BigDecimal.ROUND_UNNECESSARY);

	static private final int maxPrecision = 38;

	static public final decimal Min = new decimal(integer.Min);
	static public final decimal Max = new decimal(integer.Max);

	static public final decimal Zero = new decimal();
	static public final decimal NaN = new decimal(Double.NaN);

	private BigDecimal value;
	private boolean nan = false;

	public decimal() {
		set(new BigDecimal(0));
	}

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
		set(decimal);
	}

	public decimal(BigDecimal decimal) {
		set(decimal);
	}

	public decimal(long x) {
		set(new BigDecimal(x));
	}

	@Override
	public String toString() {
		return nan ? "NaN" : value.toPlainString();
	}

	public boolean isNaN() {
		return nan;
	}

	public BigDecimal get() {
		return value;
	}

	public double getDouble() {
		return nan ? Double.NaN : value.doubleValue();
	}

	public void set(double value) {
		if(Double.isNaN(value))
			nan = true;
		else
			set(BigDecimal.valueOf(value));
	}

	public void set(BigDecimal number) {
		value = new BigDecimal(number.unscaledValue(), number.scale());
		if(number.precision() > decimal.maxPrecision)
			value = value.setScale(number.scale() - (number.precision() - maxPrecision), RoundingMode.CEILING);
	}

	private void set(integer value) {
		set(new BigDecimal(value.get()));
	}

	private void set(decimal value) {
		set(value.get());
	}

	private void set(String strConst) {
		set(parse(strConst));
	}

	@Override
	public FieldType type() {
		return FieldType.Decimal;
	}

	@Override
	public String toDbConstant(DatabaseVendor dbtype) {
		switch(dbtype) {
		case SqlServer:
			int scale = (scale() == 0 ? 1 : scale());
			int precision = ((precision() + scale) > maxPrecision ? maxPrecision - scale : precision()) + scale;
			return "CONVERT([numeric](" + Integer.toString(precision) + "," + Integer.toString(scale) + "),(" + toString() + "),(0))";
		case Postgres:
			return nan ? "double precision 'NaN'" : toString();
		default:
			return toString();
		}
	}

	@Override
	public int hashCode() {
		return nan ? 0 : value.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof decimal))
			return false;

		decimal d = (decimal) o;
		return value == d.value || value != null && value.equals(d.value);
	}

	@Override
	public int compareTo(primary primary) {
		if(primary instanceof decimal) {
			decimal decimal = (decimal)primary;
			if(nan)
				return decimal.nan ? 0 : 1;
			if(decimal.nan)
				return -1;
			return value.compareTo(decimal.value);
		}

		if(primary instanceof integer) {
			if(nan)
				return 1;
			integer integer = (integer)primary;
			return value.compareTo(integer.decimal().value);
		}

		return -1;
	}

	public int precision() {
		return nan ? 0 : value.precision();
	}

	public int scale() {
		return nan ? 0 : value.scale();
	}

	public sql_decimal sql_decimal() {
		return new sql_decimal(this);
	}

	public decimal operatorAdd() {
		return this;
	}

	public decimal operatorSub() {
		return nan ? NaN : new decimal(value.negate());
	}

	public decimal operatorAdd(integer x) {
		return nan ? NaN : new decimal(value.add(new BigDecimal(x.get())));
	}

	public decimal operatorSub(integer x) {
		return nan ? NaN : new decimal(value.subtract(new BigDecimal(x.get())));
	}

	public decimal operatorMul(integer x) {
		return nan ? NaN : new decimal(value.multiply(new BigDecimal(x.get())));
	}

	public decimal operatorMod(integer x) {
		try {
			return nan ? NaN : new decimal(value.divideAndRemainder(new BigDecimal(x.get()))[1]);
		} catch(ArithmeticException e) {
			throw new RuntimeException(e);
		}
	}

	public decimal operatorAdd(decimal x) {
		return nan || x.nan ? NaN : new decimal(value.add(x.value));
	}

	public decimal operatorSub(decimal x) {
		return nan || x.nan ? NaN : new decimal(value.subtract(x.value));
	}

	public decimal operatorMul(decimal x) {
		return nan || x.nan ? NaN : new decimal(value.multiply(x.value));
	}

	public decimal operatorDiv(integer x) {
		return nan ? NaN : operatorDiv(new decimal(x));
	}

	public decimal operatorDiv(decimal x) {
		if(nan || x.nan)
			return NaN;

		BigDecimal divisor = x.get();

		MathContext mc = new MathContext((int)Math.min(precision() + (long)Math.ceil(10.0 * divisor.precision() / 3.0), Integer.MAX_VALUE));

		try {
			return new decimal(value.divide(divisor, mc));
		} catch(ArithmeticException e) {
			throw new RuntimeException(e);
		}
	}

	public decimal operatorMod(decimal x) {
		if(nan || x.nan)
			return NaN;

		try {
			return new decimal(value.divideAndRemainder(x.get())[1]);
		} catch(ArithmeticException e) {
			throw new RuntimeException(e);
		}
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
		return new bool(equals(x));
	}

	public bool operatorNotEqu(decimal x) {
		return new bool(!equals(x));
	}

	public bool operatorLess(decimal x) {
		return new bool(compareTo(x) < 0);
	}

	public bool operatorMore(decimal x) {
		return new bool(compareTo(x) > 0);
	}

	public bool operatorLessEqu(decimal x) {
		return new bool(compareTo(x) <= 0);
	}

	public bool operatorMoreEqu(decimal x) {
		return new bool(compareTo(x) >= 0);
	}

	public decimal z8_abs() {
		return nan ? NaN : new decimal(Math.abs(value.doubleValue()));
	}

	public integer z8_signum() {
		if(nan)
			throw new UnsupportedOperationException("signum(NaN)");
		return new integer((long)Math.signum(value.doubleValue()));
	}

	public integer z8_ceil() {
		if(nan)
			throw new UnsupportedOperationException("ceil(NaN)");
		return new integer((long)Math.ceil(value.doubleValue()));
	}

	public integer z8_floor() {
		if(nan)
			throw new UnsupportedOperationException("ceil(NaN)");
		return new integer((long)Math.floor(value.doubleValue()));
	}

	public integer round() {
		if(nan)
			throw new UnsupportedOperationException("ceil(NaN)");
		return new integer(Math.round(value.doubleValue()));
	}

	public decimal round(int digits) {
		return round(digits, RoundHalfUp);
	}

	public decimal round(int digits, integer mode) {
		return nan ? NaN : new decimal(value.setScale(digits, RoundingMode.valueOf(mode.getInt())));
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
			return nan ? NaN : new decimal(Math.sin(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_cos() {
		try {
			return nan ? NaN : new decimal(Math.cos(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_tan() {
		try {
			return nan ? NaN : new decimal(Math.tan(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_asin() {
		try {
			return nan ? NaN : new decimal(Math.asin(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_acos() {
		try {
			return nan ? NaN : new decimal(Math.acos(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_atan() {
		try {
			return nan ? NaN : new decimal(Math.atan(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_sinh() {
		try {
			return nan ? NaN : new decimal(Math.sinh(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_cosh() {
		try {
			return nan ? NaN : new decimal(Math.cosh(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_tanh() {
		try {
			return nan ? NaN : new decimal(Math.tanh(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_toRadians() {
		try {
			return nan ? NaN : new decimal(Math.toRadians(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_toDegrees() {
		try {
			return nan ? NaN : new decimal(Math.toDegrees(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_exp() {
		try {
			return nan ? NaN : new decimal(Math.exp(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_ln() {
		try {
			return nan ? NaN : new decimal(Math.log(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_log10() {
		try {
			return nan ? NaN : new decimal(Math.log10(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_sqrt() {
		try {
			return nan ? NaN : new decimal(Math.sqrt(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_cbrt() {
		try {
			return nan ? NaN : new decimal(Math.cbrt(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_power(decimal power) {
		return nan || power.nan ? NaN : new decimal(Math.pow(value.doubleValue(), power.get().doubleValue()));
	}

	public decimal z8_power(integer power) {
		return nan ? NaN : new decimal(Math.pow(value.doubleValue(), power.get()));
	}

	static public decimal parse(String value) {
		try {
			if(value.isEmpty())
				return decimal.Zero;
			return new decimal(new BigDecimal(value));
		} catch(NumberFormatException e) {
			throw new RuntimeException("Invalid value for decimal: '" + value + "'");
		}
	}

	static public decimal z8_parse(string value) {
		return parse(value.get());
	}
}
