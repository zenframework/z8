package org.zenframework.z8.server.db.sql.expressions;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.sql.SqlToken;

abstract public class Expression extends SqlToken {
	protected Operation operation;
	protected SqlToken left;
	protected SqlToken right;

	public Expression(SqlToken left, Operation operation, SqlToken right) {
		this.left = left;
		this.operation = operation;
		this.right = right;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		if(left != null)
			left.collectFields(fields);

		if(right != null)
			right.collectFields(fields);
	}
}
