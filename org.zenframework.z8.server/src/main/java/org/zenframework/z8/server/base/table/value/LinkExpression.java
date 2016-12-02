package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.ITable;
import org.zenframework.z8.server.db.generator.IForeignKey;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;

public class LinkExpression extends GuidExpression implements ILink, IForeignKey {
	public static class CLASS<T extends LinkExpression> extends GuidExpression.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(LinkExpression.class);
			setForeignKey(false);
			setSystem(true);
		}

		@Override
		public Object newObject(IObject container) {
			return new LinkExpression(container);
		}
	}

	private Query.CLASS<Query> query = null;

	private boolean writeLinkMeta = true;

	public LinkExpression(IObject container) {
		super(container);
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
	public boolean isDataField() {
		return false;
	}

	@Override
	public ITable getReferencedTable() {
		Query query = getQuery();
		return query instanceof ITable ? (ITable)query : null;
	}

	@Override
	public IField getFieldDescriptor() {
		return this;
	}

	@Override
	public IField getReferer() {
		return getQuery().primaryKey();
	}

	@Override
	public void writeMeta(JsonWriter writer, Query query) {
		super.writeMeta(writer, query);

		if(!writeLinkMeta)
			return;

		if(!query.getPath(this).isEmpty())
			return;

		writer.writeProperty(Json.isLink, true);
		writer.startObject(Json.query);

		query = getQuery();

		if(query != null) {
			writer.writeProperty(Json.id, query.id());
			writer.writeProperty(Json.primaryKey, query.primaryKey().id());
			writer.writeProperty(Json.text, query.displayName());
			writer.writeProperty(Json.icon, query.icon());
		}

		writer.finishObject();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void operatorAssign(Query.CLASS<? extends Query> data) {
		query = (Query.CLASS)data;
	}

	@Override
	public Join getJoin() {
		return Join.Left;
	}
}
