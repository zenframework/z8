package org.zenframework.z8.server.types.sql;

import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Bitwise;
import org.zenframework.z8.server.db.sql.expressions.Group;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.expressions.UnaryNot;
import org.zenframework.z8.server.db.sql.functions.bitset.Cardinality;
import org.zenframework.z8.server.db.sql.functions.conversion.ToString;
import org.zenframework.z8.server.db.sql.functions.string.Length;
import org.zenframework.z8.server.types.bitset;

public class sql_bitset extends sql_primary {

	public sql_bitset() {
		this(new bitset());
	}

	public sql_bitset(bitset value) {
		super(new SqlConst(value));
	}

	public sql_bitset(SqlToken token) {
		super(token);
	}

	@Override
	public sql_string z8_toString() {
		return new sql_string(new ToString(this));
	}

	public sql_bitset operatorPriority() {
		return new sql_bitset(new Group(this));
	}

	public sql_bitset operatorNot() {
		return new sql_bitset(new UnaryNot(this));
	}

	public sql_bitset operatorBitwiseAnd(SqlToken value) {
		return new sql_bitset(new Bitwise(this, Operation.BitwiseAnd, value));
	}

	public sql_bitset operatorOr(SqlToken value) {
		return new sql_bitset(new Bitwise(this, Operation.BitwiseOr, value));
	}

	public sql_bitset operatorXor(SqlToken value) {
		return new sql_bitset(new Bitwise(this, Operation.Xor, value));
	}

	public sql_bool operatorEqu(SqlToken value) {
		return new sql_bool(new Rel(this, Operation.Eq, value));
	}

	public sql_bool operatorNotEqu(SqlToken value) {
		return new sql_bool(new Rel(this, Operation.NotEq, value));
	}

	public sql_integer z8_size() {
		return new sql_integer(new Length(this));
	}

	public sql_integer z8_cardinality() {
		return new sql_integer(new Cardinality(this));
	}
}
