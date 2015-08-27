package org.zenframework.z8.server.base.model.actions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.ILink;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;

public class ActionParameters {
    public String requestId;

    public Query requestQuery;

    public Query query;
    public Collection<Field> fields;
    public Collection<Field> sortFields;
    public Collection<Field> groupFields;
    public Collection<Field> groupBy;
    public Collection<Link> aggregateBy;
    public Field totalsBy;

    public Map<String, String> requestParameters = new HashMap<String, String>();

    public Field keyField;
    public ILink link;

    public ActionParameters() {}

    public ActionParameters(Query query) {
        this.query = query;
    }

    public ActionParameters(Query query, Collection<Field> fields) {
        this(query);
        this.fields = fields;
    }

    public guid getId() {
        String id = requestParameters.get(Json.id);
        return id != null ? new guid(id) : null;
    }
    
    public guid getRecordId() {
        String recordId = requestParameters.get(Json.recordId);
        return recordId != null ? new guid(recordId) : null;
    }

    public guid getGuid(String key) {
        return new guid(requestParameters.get(key));
    }

    public boolean getBoolean(String key) {
        return new bool(requestParameters.get(key)).get();
    }
}
