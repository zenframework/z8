package org.zenframework.z8.server.types;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.types.sql.sql_integer;

public final class integer extends primary {

	private static final long serialVersionUID = 2882942660543308166L;

	static public integer Min = new integer(Long.MIN_VALUE + 1); // 0x8000000000000000
																	// + 1 =
																	// -0x7fffffffffffffff
	static public integer Max = new integer(Long.MAX_VALUE); // 0x7fffffffffffffff

	static public integer Zero = new integer();
	static public integer One = new integer(1);
	static public integer MinusOne = new integer(-1);

	private long value = 0;

	public integer() {
	}

	public integer(integer x) {
		set(x.value);
	}

	public integer(long x) {
		set(x);
	}

	public integer(decimal x) {
		set(x.get().longValue());
	}

	public integer(string x) {
		this(x.get());
	}

	public integer(String x) {
		set(parse(x));
	}

	@Override
	public String toString() {
		return format(10);
	}

	static public integer parse(String value) {
		return parse(value, 10);
	}

	static public integer parse(String value, int radix) {
		if(value == null || value.isEmpty())
			return integer.Zero;

		char lastChar = value.charAt(value.length() - 1);
		if(lastChar == 'L' || lastChar == 'l')
			value = value.substring(0, value.length() - 1);

		return new integer(Long.parseLong(value, radix));
	}

	public String format(int radix) {
		return Long.toString(value, radix);
	}

	public long get() {
		return value;
	}

	public int getInt() {
		return (int)value;
	}

	private void set(long x) {
		value = x;
	}

	private void set(integer x) {
		set(x.get());
	}

	@Override
	public FieldType type() {
		return FieldType.Integer;
	}

	@Override
	public String toDbConstant(DatabaseVendor dbtype) {
		switch(dbtype) {
		case SqlServer:
			return toString();
		default:
			return toString();
		}
	}

	@Override
	public int hashCode() {
		return new Long(value).hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if(object instanceof integer) {
			return value == ((integer)object).value;
		}
		return false;
	}

	@Override
	public int compareTo(primary primary) {
		if(primary instanceof decimal) {
			decimal decimal = (decimal)primary;
			return (int)Math.signum(value - decimal.round().value);
		}

		if(primary instanceof integer) {
			integer integer = (integer)primary;
			return (int)Math.signum(value - integer.value);
		}

		return -1;
	}

	public decimal decimal() {
		return new decimal(this);
	}

	public sql_integer sql_int() {
		return new sql_integer(this);
	}

	public integer operatorAnd(integer x) {
		return new integer(value & x.value);
	}

	public integer operatorOr(integer x) {
		return new integer(value | x.value);
	}

	public integer operatorXor(integer x) {
		return new integer(value ^ x.value);
	}

	public integer operatorNot() {
		return new integer(~value);
	}

	public integer operatorAdd() {
		return this;
	}

	public integer operatorSub() {
		return new integer(-value);
	}

	public integer operatorAdd(integer x) {
		return new integer(value + x.value);
	}

	public integer operatorSub(integer x) {
		return new integer(value - x.value);
	}

	public integer operatorMul(integer x) {
		return new integer(value * x.value);
	}

	public integer operatorDiv(integer x) {
		try {
			return new integer(value / x.value);
		} catch(ArithmeticException e) {
			throw new exception(e);
		}
	}

	public integer operatorMod(integer x) {
		try {
			return new integer(value % x.value);
		} catch(ArithmeticException e) {
			throw new exception(e);
		}
	}

	public decimal operatorAdd(decimal x) {
		return new decimal(this).operatorAdd(x);
	}

	public decimal operatorSub(decimal x) {
		return new decimal(this).operatorSub(x);
	}

	public decimal operatorMul(decimal x) {
		return new decimal(this).operatorMul(x);
	}

	public decimal operatorDiv(decimal x) {
		return new decimal(this).operatorDiv(x);
	}

	public decimal operatorMod(decimal x) {
		return new decimal(this).operatorMod(x);
	}

	public bool operatorEqu(integer x) {
		return new bool(value == x.value);
	}

	public bool operatorNotEqu(integer x) {
		return new bool(value != x.value);
	}

	public bool operatorLess(integer x) {
		return new bool(value < x.value);
	}

	public bool operatorMore(integer x) {
		return new bool(value > x.value);
	}

	public bool operatorLessEqu(integer x) {
		return new bool(value <= x.value);
	}

	public bool operatorMoreEqu(integer x) {
		return new bool(value >= x.value);
	}

	public bool operatorEqu(decimal x) {
		return x.operatorEqu(this);
	}

	public bool operatorNotEqu(decimal x) {
		return x.operatorNotEqu(this);
	}

	public bool operatorLess(decimal x) {
		return x.operatorMoreEqu(this);
	}

	public bool operatorMore(decimal x) {
		return x.operatorLessEqu(this);
	}

	public bool operatorLessEqu(decimal x) {
		return x.operatorMore(this);
	}

	public bool operatorMoreEqu(decimal x) {
		return x.operatorLess(this);
	}

	public integer z8_abs() {
		return new integer(Math.abs(value));
	}

	public integer z8_signum() {
		return new integer((long)Math.signum(value));
	}

	public integer z8_not() {
		return operatorNot();
	}

	public integer z8_and(integer x) {
		return operatorAnd(x);
	}

	public integer z8_or(integer x) {
		return operatorOr(x);
	}

	public integer z8_xor(integer x) {
		return operatorXor(x);
	}

	public decimal z8_sin() {
		return new decimal(Math.sin(value));
	}

	public decimal z8_cos() {
		return new decimal(Math.cos(value));
	}

	public decimal z8_tan() {
		return new decimal(Math.tan(value));
	}

	public decimal z8_asin() {
		return new decimal(Math.asin(value));
	}

	public decimal z8_acos() {
		return new decimal(Math.acos(value));
	}

	public decimal z8_atan() {
		return new decimal(Math.atan(value));
	}

	public decimal z8_sinh() {
		return new decimal(Math.sinh(value));
	}

	public decimal z8_cosh() {
		return new decimal(Math.cosh(value));
	}

	public decimal z8_tanh() {
		return new decimal(Math.tanh(value));
	}

	public decimal z8_toRadians() {
		return new decimal(Math.toRadians(value));
	}

	public decimal z8_toDegrees() {
		return new decimal(Math.toDegrees(value));
	}

	public decimal z8_exp() {
		return new decimal(Math.exp(value));
	}

	public decimal z8_ln() {
		return new decimal(Math.log(value));
	}

	public decimal z8_log10() {
		return new decimal(Math.log10(value));
	}

	public decimal z8_sqrt() {
		return new decimal(Math.sqrt(value));
	}

	public decimal z8_cbrt() {
		return new decimal(Math.cbrt(value));
	}

	public decimal z8_power(decimal power) {
		return new decimal(Math.pow(value, power.get().doubleValue()));
	}

	public integer z8_leftShift(integer _byte) {
		return new integer(value << _byte.get());
	}

	public integer z8_rightShift(integer _byte) {
		return new integer(value >> _byte.get());
	}

	public string z8_toString(integer radix) {
		return new string(format(radix.getInt()));
	}

	static public integer z8_parse(string string) {
		return parse(string != null ? string.get() : "", 10);
	}

	static public integer z8_parse(string string, integer radix) {
		return parse(string != null ? string.get() : "", radix.getInt());
	}
}
