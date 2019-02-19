package org.zenframework.z8.server.types.sql;

import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Add;
import org.zenframework.z8.server.db.sql.expressions.Group;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.functions.Count;
import org.zenframework.z8.server.db.sql.functions.Max;
import org.zenframework.z8.server.db.sql.functions.Min;
import org.zenframework.z8.server.db.sql.functions.conversion.IsIntString;
import org.zenframework.z8.server.db.sql.functions.conversion.IsNumericString;
import org.zenframework.z8.server.db.sql.functions.conversion.StringToInt;
import org.zenframework.z8.server.db.sql.functions.conversion.ToDate;
import org.zenframework.z8.server.db.sql.functions.conversion.ToDecimal;
import org.zenframework.z8.server.db.sql.functions.string.GetJson;
import org.zenframework.z8.server.db.sql.functions.string.IndexOf;
import org.zenframework.z8.server.db.sql.functions.string.IsEmpty;
import org.zenframework.z8.server.db.sql.functions.string.LPad;
import org.zenframework.z8.server.db.sql.functions.string.LTrim;
import org.zenframework.z8.server.db.sql.functions.string.Length;
import org.zenframework.z8.server.db.sql.functions.string.Like;
import org.zenframework.z8.server.db.sql.functions.string.Lower;
import org.zenframework.z8.server.db.sql.functions.string.RPad;
import org.zenframework.z8.server.db.sql.functions.string.RTrim;
import org.zenframework.z8.server.db.sql.functions.string.RegIndexOf;
import org.zenframework.z8.server.db.sql.functions.string.Replace;
import org.zenframework.z8.server.db.sql.functions.string.Reverse;
import org.zenframework.z8.server.db.sql.functions.string.Substr;
import org.zenframework.z8.server.db.sql.functions.string.Upper;
import org.zenframework.z8.server.types.string;

public class sql_string extends sql_primary {
	public sql_string() {
		super(new SqlConst(new string()));
	}

	public sql_string(String value) {
		super(new SqlConst(new string(value)));
	}

	public sql_string(string value) {
		super(new SqlConst(value));
	}

	public sql_string(SqlToken token) {
		super(token);
	}

	public sql_bool z8_isInt() {
		return new sql_bool(new IsIntString(this));
	}

	public sql_bool z8_isNumeric() {
		return new sql_bool(new IsNumericString(this));
	}

	public sql_integer z8_toInt() {
		return new sql_integer(new StringToInt(this));
	}

	public sql_decimal z8_toDecimal() {
		return new sql_decimal(new ToDecimal(this));
	}

	public sql_date z8_toDate() {
		return new sql_date(new ToDate(this));
	}

	public sql_bool isEmpty() {
		return new sql_bool(new IsEmpty(this));
	}

	public sql_bool z8_isEmpty() {
		return isEmpty();
	}

	public sql_integer z8_length() {
		return new sql_integer(new Length(this));
	}

	public sql_bool z8_contains(sql_string pattern) {
		return z8_contains(pattern, sql_integer.Zero);
	}

	public sql_bool z8_contains(sql_string pattern, sql_integer position) {
		sql_integer index = z8_indexOf(pattern, position);
		return new sql_bool(new Rel(index, Operation.NotEq, sql_integer.MinusOne));
	}

	public sql_integer z8_indexOf(sql_string pattern) {
		return z8_indexOf(pattern, null);
	}

	public sql_integer z8_indexOf(sql_string pattern, sql_integer position) {
		return new sql_integer(new IndexOf(pattern, this, position));
	}

	public sql_integer z8_patIndexOf(sql_string reg_exp) {
		return new sql_integer(new RegIndexOf(this, reg_exp));
	}

	public sql_bool z8_like(sql_string pattern) {
		return z8_like(pattern, null);
	}

	public sql_bool z8_like(sql_string pattern, sql_string escape) {
		return new sql_bool(new Like(this, pattern, escape));
	}

	public sql_string z8_toLower() {
		return new sql_string(new Lower(this));
	}

	public sql_string z8_toUpper() {
		return new sql_string(new Upper(this));
	}

	public sql_string z8_trimLeft() {
		return new sql_string(new LTrim(this));
	}

	public sql_string z8_trimRight() {
		return new sql_string(new RTrim(this));
	}

	public sql_string z8_trimAll() {
		return z8_trimLeft().z8_trimRight();
	}

	public sql_string z8_padLeft(sql_integer length) {
		return new sql_string(new LPad(this, length, null));
	}

	public sql_string z8_padLeft(sql_integer length, sql_string pattern) {
		return new sql_string(new LPad(this, length, pattern));
	}

	public sql_string z8_padRight(sql_integer length) {
		return new sql_string(new RPad(this, length, null));
	}

	public sql_string z8_padRight(sql_integer length, sql_string pattern) {
		return new sql_string(new RPad(this, length, pattern));
	}

	public sql_string z8_replace(sql_string pattern, sql_string replacement) {
		return new sql_string(new Replace(this, pattern, replacement));
	}

	public sql_string z8_substring(sql_integer position, sql_integer count) {
		return new sql_string(new Substr(this, position, count));
	}

	public sql_string z8_reverse() {
		return new sql_string(new Reverse(this));
	}

	public sql_string z8_json(sql_string name) {
		return new sql_string(new GetJson(this, name));
	}

	public sql_string z8_json(sql_integer num) {
		return new sql_string(new GetJson(this, num));
	}

	public sql_string z8_max() {
		return new sql_string(new Max(this));
	}

	public sql_string z8_min() {
		return new sql_string(new Min(this));
	}

	public sql_integer z8_count() {
		return new sql_integer(new Count(this));
	}

	public sql_string operatorPriority() {
		return new sql_string(new Group(this));
	}

	public sql_string operatorAdd(sql_string value) {
		return new sql_string(new Add(this, Operation.Add, value));
	}

	public sql_bool operatorLess(sql_string value) {
		return new sql_bool(new Rel(this, Operation.LT, value));
	}

	public sql_bool operatorMore(sql_string value) {
		return new sql_bool(new Rel(this, Operation.GT, value));
	}

	public sql_bool operatorLessEqu(sql_string value) {
		return new sql_bool(new Rel(this, Operation.LE, value));
	}

	public sql_bool operatorMoreEqu(sql_string value) {
		return new sql_bool(new Rel(this, Operation.GE, value));
	}

	public sql_bool operatorEqu(sql_string value) {
		return new sql_bool(new Rel(this, Operation.Eq, value));
	}

	public sql_bool operatorNotEqu(sql_string value) {
		return new sql_bool(new Rel(this, Operation.NotEq, value));
	}

	@Override
	public sql_string z8_toString() {
		return this;
	}
}
