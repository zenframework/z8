package org.zenframework.z8.server.db.sql.expressions;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlConst;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.functions.conversion.ToNumber;
import org.zenframework.z8.server.db.sql.functions.numeric.Round;
import org.zenframework.z8.server.types.integer;

abstract public class Expression extends SqlToken {
	protected Operation operation;
	protected SqlToken left;
	protected SqlToken right;

	public Expression(SqlToken left, Operation operation, SqlToken right) {
		this.left = left;
		setNewExpression(operation, right);
	}

	public Operation getSQLOperation() {
		return operation;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		if(left != null) {
			left.collectFields(fields);
		}

		if(right != null) {
			right.collectFields(fields);
		}
	}

	public SqlToken getSQLRight() {
		return right;
	}

	private void setNewExpression(Operation operation, SqlToken right) {
		this.operation = operation;

		if(isDateSpanWithDate(left, right))
			left = convertDateSpanToDate(left);
		if(isDateSpanWithDate(right, left))
			this.right = convertDateSpanToDate(right);
		else
			this.right = right;
	}

	private boolean isDateSpanWithDate(SqlToken left, SqlToken right) {
		return left != null && right != null && left.type() == FieldType.Datespan && (right.type() == FieldType.Date || right.type() == FieldType.Datetime);
	}

	private SqlToken convertDateSpanToDate(SqlToken t) {
		integer hourInDay = new integer(24 * 60 * 60 * 1000);
		return new DateSpanRound(new Mul(new ToNumber(t), Operation.Div, new SqlConst(hourInDay)), new SqlConst(new integer(5)));
	}

	private class DateSpanRound extends Round {
		DateSpanRound(SqlToken p1, SqlToken p2) {
			super(p1, p2);
		}

		@Override
		public FieldType type() {
			return FieldType.Datespan;
		}
	}
}
