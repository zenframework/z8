package org.zenframework.z8.server.base.table.value;

import java.util.Collection;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.ITable;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.db.generator.IForeignKey;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.sql.sql_bool;

public class Link extends GuidField implements ILink, IForeignKey {
	public static class CLASS<T extends Link> extends GuidField.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Link.class);
			setForeignKey(true);
		}

		@Override
		public Object newObject(IObject container) {
			return new Link(container);
		}
	}

	public JoinType join = JoinType.Inner;

	private Query.CLASS<Query> query = null;

	public Link(IObject container) {
		super(container);
		setSystem(true);
		indexed = bool.True;
	}

	@Override
	public Query.CLASS<Query> query() {
		return query;
	}

	@Override
	public Query getQuery() {
		return query != null ? query.get() : null;
	}

	@Override
	public JoinType getJoin() {
		return join;
	}

	@Override
	public sql_bool on() {
		SqlToken on = new Equ(sql_guid(), query.get().primaryKey());
		return new sql_bool(on);
	}

	@Override
	public boolean isDataField() {
		return false;
	}

	@Override
	public ITable getReferencedTable() {
		Query query = getQuery();
		if(query instanceof ITable)
			return (ITable)query;

		if(query == null && isParentKey())
			return (Table)getOwner();

		return null;
	}

	@Override
	public IField getFieldDescriptor() {
		return this;
	}

	@Override
	public IField getReferer() {
		return ((Table)getReferencedTable()).primaryKey();
	}

	@Override
	protected primary getNullValue() {
		return getReferer().get();
	}

	@Override
	public void writeMeta(JsonWriter writer, Query query, Query context) {
		if(this.query == null && !isParentKey())
			throw new RuntimeException("Link.query is null : displayName: '"  + displayName() + "'; name: '" + name() + "'");

		writer.writeProperty(Json.isLink, true);

		super.writeMeta(writer, query, context);

		Collection<ILink> path = query.getPath(this);

		if(path == null || !path.isEmpty())
			return;

		writer.startObject(Json.query);

		query = ((query = getQuery()) == null) ? owner() : query;

		writer.writeProperty(Json.id, query.id());
		writer.writeProperty(Json.primaryKey, query.primaryKey().id());
		writer.writeProperty(Json.text, query.displayName());
		writer.writeProperty(Json.icon, query.icon());

		writer.finishObject();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void operatorAssign(Query.CLASS<? extends Query> data) {
		query = (Query.CLASS)data;
	}
}
