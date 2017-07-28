package org.zenframework.z8.server.types.sql;

import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Add;
import org.zenframework.z8.server.db.sql.expressions.Group;
import org.zenframework.z8.server.db.sql.expressions.Mul;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.expressions.Unary;
import org.zenframework.z8.server.db.sql.functions.Average;
import org.zenframework.z8.server.db.sql.functions.Count;
import org.zenframework.z8.server.db.sql.functions.Max;
import org.zenframework.z8.server.db.sql.functions.Min;
import org.zenframework.z8.server.db.sql.functions.Sum;
import org.zenframework.z8.server.db.sql.functions.conversion.IntToDecimal;
import org.zenframework.z8.server.db.sql.functions.conversion.ToString;
import org.zenframework.z8.server.db.sql.functions.numeric.Abs;
import org.zenframework.z8.server.db.sql.functions.numeric.Floor;
import org.zenframework.z8.server.db.sql.functions.numeric.Power;
import org.zenframework.z8.server.db.sql.functions.numeric.Sign;
import org.zenframework.z8.server.types.integer;

public class sql_integer extends sql_primary {
	static public sql_integer Zero = new sql_integer(integer.Zero);
	static public sql_integer One = new sql_integer(integer.One);
	static public sql_integer MinusOne = new sql_integer(integer.MinusOne);

	public sql_integer() {
		this(integer.Zero);
	}

	public sql_integer(int value) {
		this(new integer(value));
	}

	public sql_integer(integer value) {
		super(new SqlConst(value));
	}

	public sql_integer(SqlToken token) {
		super(token);
	}

	@Override
	public sql_string z8_toString() {
		return new sql_string(new ToString(this));
	}

	public sql_integer z8_sign() {
		return new sql_integer(new Sign(this));
	}

	public sql_integer z8_abs() {
		return new sql_integer(new Abs(this));
	}

	public sql_integer z8_power(sql_integer power) {
		return new sql_integer(new Power(this, power));
	}

	public sql_integer z8_max() {
		return new sql_integer(new Max(this));
	}

	public sql_integer z8_min() {
		return new sql_integer(new Min(this));
	}

	public sql_integer z8_count() {
		return new sql_integer(new Count(this));
	}

	public sql_decimal z8_average() {
		return new sql_decimal(new Average(this));
	}

	public sql_integer z8_sum() {
		return new sql_integer(new Sum(this));
	}

	public sql_integer operatorPriority() {
		return new sql_integer(new Group(this));
	}

	public sql_decimal sql_decimal() {
		return new sql_decimal(new IntToDecimal(this));
	}

	public sql_integer operatorSub() {
		return new sql_integer(new Unary(Operation.Minus, this));
	}

	public sql_integer operatorAdd() {
		return this;
	}

	public sql_integer operatorAdd(sql_integer value) {
		return new sql_integer(new Add(this, Operation.Add, value));
	}

	public sql_integer operatorSub(sql_integer value) {
		return new sql_integer(new Add(this, Operation.Sub, value));
	}

	public sql_integer operatorMul(sql_integer value) {
		return new sql_integer(new Mul(this, Operation.Mul, value));
	}

	public sql_integer operatorDiv(sql_integer value) {
		return new sql_integer(new Floor(new Mul(this, Operation.Div, value)));
	}

	public sql_integer operatorMod(sql_integer value) {
		return new sql_integer(new Mul(this, Operation.Mod, value));
	}

	public sql_decimal operatorAdd(sql_decimal value) {
		return new sql_decimal(new Add(this, Operation.Add, value));
	}

	public sql_decimal operatorSub(sql_decimal value) {
		return new sql_decimal(new Add(this, Operation.Sub, value));
	}

	public sql_decimal operatorMul(sql_decimal value) {
		return new sql_decimal(new Mul(this, Operation.Mul, value));
	}

	public sql_decimal operatorDiv(sql_decimal value) {
		return new sql_decimal(new Mul(this, Operation.Div, value));
	}

	public sql_decimal operatorMod(sql_decimal value) {
		return new sql_decimal(new Mul(this, Operation.Mod, value));
	}

	public sql_bool operatorLess(SqlToken value) {
		return new sql_bool(new Rel(this, Operation.LT, value));
	}

	public sql_bool operatorMore(SqlToken value) {
		return new sql_bool(new Rel(this, Operation.GT, value));
	}

	public sql_bool operatorLessEqu(SqlToken value) {
		return new sql_bool(new Rel(this, Operation.LE, value));
	}

	public sql_bool operatorMoreEqu(SqlToken value) {
		return new sql_bool(new Rel(this, Operation.GE, value));
	}

	public sql_bool operatorEqu(SqlToken value) {
		return new sql_bool(new Rel(this, Operation.Eq, value));
	}

	public sql_bool operatorNotEqu(SqlToken value) {
		return new sql_bool(new Rel(this, Operation.NotEq, value));
	}
}
