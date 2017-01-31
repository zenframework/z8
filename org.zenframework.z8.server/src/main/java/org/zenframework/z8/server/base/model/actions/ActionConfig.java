package org.zenframework.z8.server.base.model.actions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.ILink;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class ActionConfig {
	public String requestId;

	public Query contextQuery;

	public Query query;
	public Collection<Field> fields;
	public Collection<Field> sortFields;
	public Collection<Field> groupFields;

	private Map<string, string> requestParameters = new HashMap<string, string>();

	public ILink link;

	public ActionConfig() {
	}

	public ActionConfig(Map<string, string> requestParameters) {
		this.requestParameters = requestParameters;
	}

	public ActionConfig(Query query) {
		this.query = contextQuery = query;
	}

	public ActionConfig(Query query, Collection<Field> fields) {
		this(query);
		this.fields = fields;
	}

	public Map<string, string> requestParameters() {
		return requestParameters;
	}

	public String requestParameter(string key) {
		string value = requestParameters.get(key);
		return value != null ? value.get() : null;
	}

	public guid getId() {
		String id = requestParameter(Json.id);
		return id != null ? new guid(id) : null;
	}

	public guid getRecordId() {
		String recordId = requestParameter(Json.recordId);
		return recordId != null ? new guid(recordId) : null;
	}

	public guid getGuid(string key) {
		return new guid(requestParameter(key));
	}

	public boolean getBoolean(string key) {
		return new bool(requestParameter(key)).get();
	}
}
