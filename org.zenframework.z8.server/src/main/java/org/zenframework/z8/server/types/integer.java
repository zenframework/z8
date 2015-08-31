package org.zenframework.z8.server.types;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.types.sql.sql_integer;

public final class integer extends primary {
    private static final long serialVersionUID = -4008043894306519115L;

    static public integer MIN_VALUE = new integer(Long.MIN_VALUE + 1); // 0x8000000000000000  + 1 = -0x7fffffffffffffff
    static public integer MAX_VALUE = new integer(Long.MAX_VALUE); // 0x7fffffffffffffff

    private long value = 0;

    public integer() {}

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
        set(z8_parse(new string(x)));
    }

    @Override
    public integer defaultValue() {
        return new integer();
    }

    @Override
    public String toString() {
        return format(10);
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

    public void set(long x) {
        value = x;
    }

    public void set(integer x) {
        set(x.get());
    }

    public void increase(integer delta) {
        set(get() + delta.get());
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
    public String toDbString(DatabaseVendor dbtype) {
        return toDbConstant(dbtype);
    }

    @Override
    public int hashCode() {
        return new Long(value).hashCode();
    }

    @Override
    public boolean equals(Object l) {
        if(l instanceof integer) {
            return value == ((integer)l).value;
        }
        return false;
    }

    @Override
    public decimal decimal() {
        return new decimal(this);
    }

    public sql_integer sql_int() {
        return new sql_integer(this);
    }

    public void operatorAssign(integer x) {
        set(x);
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
        }
        catch(ArithmeticException e) {
            throw new exception(e);
        }
    }

    public integer operatorMod(integer x) {
        try {
            return new integer(value % x.value);
        }
        catch(ArithmeticException e) {
            throw new exception(e);
        }
    }

    public void operatorModAssign(integer x) {
        set(operatorMod(x));
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

    public integer operatorAddAssign(integer x) {
        set(value + x.value);
        return this;
    }

    public integer operatorSubAssign(integer x) {
        set(value - x.value);
        return this;
    }

    public integer operatorMulAssign(integer x) {
        set(value * x.value);
        return this;
    }

    public integer operatorDivAssign(integer x) {
        try {
            set(value / x.value);
            return this;
        }
        catch(ArithmeticException e) {
            throw new exception(e);
        }
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
        return z8_parse(string, new integer(10));
    }

    static public integer z8_parse(string string, integer radix) {
        try {
            String v = string.get();

            if(v.trim().length() == 0)
                v = "0";

            return new integer(Long.parseLong(v, radix.getInt()));
        }
        catch(NumberFormatException e) {
            throw new exception(e);
        }
    }
}
