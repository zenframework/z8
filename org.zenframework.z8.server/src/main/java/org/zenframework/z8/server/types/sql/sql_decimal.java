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
import org.zenframework.z8.server.db.sql.functions.conversion.ToChar;
import org.zenframework.z8.server.db.sql.functions.numeric.Abs;
import org.zenframework.z8.server.db.sql.functions.numeric.Ceil;
import org.zenframework.z8.server.db.sql.functions.numeric.Exp;
import org.zenframework.z8.server.db.sql.functions.numeric.Floor;
import org.zenframework.z8.server.db.sql.functions.numeric.Ln;
import org.zenframework.z8.server.db.sql.functions.numeric.Power;
import org.zenframework.z8.server.db.sql.functions.numeric.Round;
import org.zenframework.z8.server.db.sql.functions.numeric.Sign;
import org.zenframework.z8.server.db.sql.functions.numeric.Truncate;
import org.zenframework.z8.server.types.decimal;

public class sql_decimal extends sql_primary {
	public sql_decimal() {
		super(new SqlConst(new decimal()));
	}

	public sql_decimal(decimal value) {
		super(new SqlConst(value));
	}

	public sql_decimal(SqlToken token) {
		super(token);
	}

	@Override
	public sql_string z8_toString() {
		return new sql_string(new ToChar(this));
	}

	public sql_integer z8_sign() {
		return new sql_integer(new Sign(this));
	}

	public sql_decimal z8_abs() {
		return new sql_decimal(new Abs(this));
	}

	public sql_integer z8_ceil() {
		return new sql_integer(new Ceil(this));
	}

	public sql_integer z8_floor() {
		return new sql_integer(new Floor(this));
	}

	public sql_decimal z8_exp() {
		return new sql_decimal(new Exp(this));
	}

	public sql_decimal z8_ln() {
		return new sql_decimal(new Ln(this));
	}

	public sql_decimal z8_power(sql_integer power) {
		return new sql_decimal(new Power(this, power));
	}

	public sql_decimal z8_round(sql_integer digits) {
		return new sql_decimal(new Round(this, digits));
	}

	public sql_decimal z8_truncate(sql_integer digits) {
		return new sql_decimal(new Truncate(this, digits));
	}

	public sql_decimal z8_max() {
		return new sql_decimal(new Max(this));
	}

	public sql_decimal z8_min() {
		return new sql_decimal(new Min(this));
	}

	public sql_integer z8_count() {
		return new sql_integer(new Count(this));
	}

	public sql_decimal z8_average() {
		return new sql_decimal(new Average(this));
	}

	public sql_decimal z8_sum() {
		return new sql_decimal(new Sum(this));
	}

	public sql_decimal operatorPriority() {
		return new sql_decimal(new Group(this));
	}

	public sql_date operatorAdd(sql_datespan value) {
		return new sql_date(new Add(this, Operation.Add, value));
	}

	public sql_decimal operatorSub() {
		return new sql_decimal(new Unary(Operation.Minus, this));
	}

	public sql_decimal operatorAdd() {
		return this;
	}

	public sql_decimal operatorAdd(SqlToken value) {
		return new sql_decimal(new Add(this, Operation.Add, value));
	}

	public sql_decimal operatorSub(SqlToken value) {
		return new sql_decimal(new Add(this, Operation.Sub, value));
	}

	public sql_decimal operatorMul(SqlToken value) {
		return new sql_decimal(new Mul(this, Operation.Mul, value));
	}

	public sql_decimal operatorDiv(SqlToken value) {
		return new sql_decimal(new Mul(this, Operation.Div, value));
	}

	public sql_decimal operatorMod(SqlToken value) {
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
