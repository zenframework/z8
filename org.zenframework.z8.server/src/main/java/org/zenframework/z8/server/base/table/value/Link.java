package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.ITable;
import org.zenframework.z8.server.db.generator.IForeignKey;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.runtime.IObject;

public class Link extends GuidField implements ILink, IForeignKey {
    public static class CLASS<T extends Link> extends GuidField.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(Link.class);
            setAttribute(Native, Link.class.getCanonicalName());
            setForeignKey(true);
        }

        @Override
        public Object newObject(IObject container) {
            return new Link(container);
        }
    }

    public Join join = Join.Left;
    
    public Link.CLASS<? extends Link> filter = null;

    private Query.CLASS<Query> query = null;

    public Link(IObject container) {
        super(container);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void onInitialized() {
        super.onInitialized();

        if(filter != null) {
            filter.addReference((CLASS)getCLASS());
        }
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
    public Join getJoin() {
        return join;
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void operatorAssign(Query.CLASS<? extends Query> data) {
        query = (Query.CLASS)data;
    }
}
