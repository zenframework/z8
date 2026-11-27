package org.zenframework.z8.server.types.sql;

import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Add;
import org.zenframework.z8.server.db.sql.expressions.Group;
import org.zenframework.z8.server.db.sql.expressions.Mul;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.expressions.Unary;
import org.zenframework.z8.server.db.sql.functions.conversion.ToString;
import org.zenframework.z8.server.db.sql.functions.vector.Cosine;
import org.zenframework.z8.server.db.sql.functions.vector.Euclid;
import org.zenframework.z8.server.db.sql.functions.vector.Fill;
import org.zenframework.z8.server.db.sql.functions.vector.Size;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.vector;

public class sql_vector extends sql_primary {

	public sql_vector() {
		this(new vector());
	}

	public sql_vector(vector value) {
		super(new SqlConst(value));
	}

	public sql_vector(SqlToken token) {
		super(token);
	}

	@Override
	public sql_string z8_toString() {
		return new sql_string(new ToString(this));
	}

	public sql_vector operatorPriority() {
		return new sql_vector(new Group(this));
	}

	public sql_vector operatorAdd() {
		return this;
	}

	public sql_vector operatorSub() {
		return new sql_vector(new Unary(Operation.Minus, this));
	}

	public sql_vector operatorAdd(SqlToken value) {
		return new sql_vector(new Add(this, Operation.Add, value));
	}

	public sql_vector operatorSub(SqlToken value) {
		return new sql_vector(new Add(this, Operation.Sub, value));
	}

	public sql_vector operatorMul(SqlToken value) {
		return new sql_vector(new Mul(this, Operation.Mul, value));
	}

	public sql_bool operatorEqu(SqlToken value) {
		return new sql_bool(new Rel(this, Operation.Eq, value));
	}

	public sql_bool operatorNotEqu(SqlToken value) {
		return new sql_bool(new Rel(this, Operation.NotEq, value));
	}

	public sql_integer z8_size() {
		return new sql_integer(new Size(this));
	}

	public sql_decimal z8_scalar() {
		return new sql_decimal(new Euclid(this, new Fill(new Size(this), new SqlConst(new decimal(.0)))));
	}

	public sql_decimal z8_euclid(sql_vector v) {
		return new sql_decimal(new Euclid(this, v));
	}

	public sql_decimal z8_cosine(sql_vector v) {
		return new sql_decimal(new Cosine(this, v));
	}

	public static sql_vector z8_fill(sql_integer size, sql_decimal value) {
		return new sql_vector(new Fill(size, value));
	}
}
