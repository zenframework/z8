package org.zenframework.z8.server.request.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.query.QueryUtils;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.ILink;
import org.zenframework.z8.server.db.Select;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.request.RequestTarget;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public abstract class RequestAction extends RequestTarget {
	static public final String New = "new";
	static public final String Meta = "meta";
	static public final String Create = "create";
	static public final String Copy = "copy";
	static public final String Read = "read";
	static public final String Update = "update";
	static public final String Destroy = "destroy";
	static public final String Export = "export";
	static public final String Report = "report";
	static public final String Preview = "preview";
	static public final String Action = "action";
	static public final String Attach = "attach";
	static public final String Detach = "detach";
	static public final String Content = "content";

	private ActionConfig config;

	public RequestAction(ActionConfig config) {
		super(config.requestId);

		this.config = config;
	}

	public ActionConfig getConfig() {
		return config;
	}

	public boolean isRequest() {
		return config.isRequest();
	}

	public Map<string, string> getRequestParameters() {
		return config.requestParameters();
	}

	public Query getQuery() {
		return config.query;
	}

	public Query getContextQuery() {
		return config.contextQuery;
	}

	public ILink getLink() {
		return config.link;
	}

	public boolean hasRequestParameter(string key) {
		return getRequestParameter(key) != null;
	}

	public String getRequestParameter(string key) {
		return config.requestParameter(key);
	}

	public int getRequestParameter(string key, int defaultValue) {
		String value = getRequestParameter(key);
		return value != null && !value.isEmpty() ? Integer.parseInt(value) : defaultValue;
	}

	public boolean getRequestParameter(string key, boolean defaultValue) {
		String value = getRequestParameter(key);
		return value != null && !value.isEmpty() ? new bool(value).get() : defaultValue;
	}

	public guid getRecordId() {
		return getConfig().getRecordId();
	}

	public Collection<guid> getRecordIds() {
		return getConfig().getRecordIds();
	}

	public guid getParentId() {
		String parentId = getRequestParameter(Json.parentId);
		return parentId == null ? null : parentId.isEmpty() ? guid.Null : new guid(parentId);
	}

	public Collection<String> getLookupFields() {
		String lookupFields = getRequestParameter(Json.lookupFields);

		Collection<String> result = new ArrayList<String>();

		if(lookupFields == null)
			return result;

		JsonArray array = new JsonArray(lookupFields);

		for(int index = 0; index < array.length(); index++)
			result.add(array.getString(index));

		return result;
	}

	public Collection<guid> getGuidCollection(string key) {
		return getConfig().getGuidCollection(key);
	}

	protected Collection<Field> getFormFields() {
		return getFormFields(null);
	}

	private Collection<Field> getFormFields(Query query) {
		String json = getRequestParameter(Json.fields);
		if(query == null) {
			query = getContextQuery();
			if(query == null)
				query = getQuery();
		}
		return QueryUtils.parseFormFields(query, json);
	}

	protected void writeFormFields(JsonWriter writer, Query query, Collection<guid> recordIds) {
		Field primaryKey = query.primaryKey();

		Collection<Field> fields = getFormFields(/*query*/);
		fields.add(primaryKey);
		fields.add(query.lockKey());

		ILink link = getLink();
		if(link != null)
			fields.add((Field)link);

		for(Field field : fields)
			field.reset();

		QueryUtils.setFieldValues(query, getRequestParameter(Json.values));

		ReadAction action = new ReadAction(query, fields, recordIds);
		action.addFilter(query.scope());

		Select cursor = action.getCursor();
		action.writeData(cursor, writer);
		cursor.close();
	}
}
