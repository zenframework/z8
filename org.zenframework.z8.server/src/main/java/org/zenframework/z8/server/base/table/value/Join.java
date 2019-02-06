package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.ITable;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.sql.sql_bool;

public class Join extends Expression implements IJoin {
	public static class CLASS<T extends Join> extends Expression.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Join.class);
			setSystem(true);
		}

		@Override
		public Object newObject(IObject container) {
			return new Join(container);
		}
	}

	public JoinType join = JoinType.Inner;
	public Query.CLASS<? extends Query> query = null;

	public Join(IObject container) {
		super(container);
		setSystem(true);
	}

	@Override
	public Query.CLASS<? extends Query> query() {
		return query;
	}

	@Override
	public Query getQuery() {
		return query != null ? query.get() : null;
	}

	@Override
	public ITable getReferencedTable() {
		Query query = getQuery();
		return query instanceof ITable ? (ITable)query : null;
	}

	@Override
	public IField getReferer() {
		return getQuery().primaryKey();
	}

	@Override
	public JoinType getJoin() {
		return join;
	}

	@Override
	public sql_bool on() {
		return new sql_bool(expression());
	}

	@Override
	public boolean isDataField() {
		return false;
	}

	@Override
	public void writeMeta(JsonWriter writer, Query query, Query context) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void operatorAssign(Query.CLASS<? extends Query> data) {
		query = (Query.CLASS)data;
	}

	public void operatorAssign(sql_bool expression) {
		setExpression(expression);
	}
}
