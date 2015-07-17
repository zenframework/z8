package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.ITable;
import org.zenframework.z8.server.db.generator.IForeignKey;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;

public class LinkExpression extends GuidExpression implements ILink, IForeignKey {
    public static class CLASS<T extends LinkExpression> extends GuidExpression.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(LinkExpression.class);
            setAttribute(Native, LinkExpression.class.getCanonicalName());
            setForeignKey(false);
        }

        @Override
        public Object newObject(IObject container) {
            return new LinkExpression(container);
        }
    }

    private Query.CLASS<Query> query = null;

    public LinkExpression(IObject container) {
        super(container);
        system = new bool(true);
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
    public void writeMeta(JsonObject writer) {
        super.writeMeta(writer);
        writer.put(Json.link, true);

        Query query = getQuery();

        if(query != null) {
            writer.put(Json.queryId, query.id());
            writer.put(Json.linkedVia, query.primaryKey().id());
            writer.put(Json.text, query.displayName());
        }
    }

    @SuppressWarnings("unchecked")
    public void operatorAssign(Query.CLASS<? extends Query> data) {
        query = (Query.CLASS)data;
    }

    @Override
    public Join getJoin() {
        return Join.Left;
    }
}
