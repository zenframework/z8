package org.zenframework.z8.server.db.sql.functions;

import java.util.Collection;
import java.util.LinkedList;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlToken;

public abstract class BaseCaseToken extends SqlToken {
	public class Pair<LEFT, RIGHT> {
		private LEFT left = null;
		private RIGHT right = null;

		public Pair() {
		}

		public Pair(LEFT left, RIGHT right) {
			this.left = left;
			this.right = right;
		}

		public Pair(Pair<LEFT, RIGHT> pair) {
			left = pair.left;
			right = pair.right;
		}

		public final LEFT getLeft() {
			return left;
		}

		public final void setLeft(LEFT left) {
			this.left = left;
		}

		public final RIGHT getRight() {
			return right;
		}

		public final void setRight(RIGHT right) {
			this.right = right;
		}
	}

	protected SqlToken condition = null;
	protected LinkedList<Pair<SqlToken, SqlToken>> whens = new LinkedList<Pair<SqlToken, SqlToken>>();
	protected SqlToken elseToken;

	public BaseCaseToken() {
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		if(condition != null) {
			condition.collectFields(fields);
		}

		for(Pair<SqlToken, SqlToken> pair : whens) {
			pair.getLeft().collectFields(fields);
			pair.getRight().collectFields(fields);
		}

		if(elseToken != null) {
			elseToken.collectFields(fields);
		}
	}

	public void setCondition(SqlToken condition) {
		this.condition = condition;
	}

	public void addWhen(SqlToken when, SqlToken result) {
		whens.add(new Pair<SqlToken, SqlToken>(when, result));
	}

	public void setElse(SqlToken condition) {
		elseToken = condition;
	}

	@Override
	public FieldType type() {
		return elseToken.type();
	}
}
