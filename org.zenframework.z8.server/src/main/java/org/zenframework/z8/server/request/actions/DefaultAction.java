package org.zenframework.z8.server.request.actions;

import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.types.string;

public class DefaultAction extends ReadAction {
	private boolean writeReadResponse = true;

	public DefaultAction(ActionConfig config) {
		this(config, true);
	}

	protected DefaultAction(ActionConfig config, boolean writeReadResponse) {
		super(config);
		this.writeReadResponse = writeReadResponse;
	}

	@Override
	public void writeResponse(JsonWriter writer) throws Throwable {
		ActionConfig config = getConfig();
		Map<string, string> requestParameters = getRequestParameters();

		Query query = getQuery();

		query.setSelectFields(getSelectFields());
		query.writeMeta(writer, getContextQuery());

		if(!writeReadResponse)
			return;

		writer.writeSort(config.sortFields);
		writer.writeGroup(config.groupFields);

		if(requestParameters.get(Json.start) == null)
			requestParameters.put(Json.start, new string(query.start()));
		if(requestParameters.get(Json.limit) == null)
			requestParameters.put(Json.limit, new string(query.limit()));

		super.writeResponse(writer);
	}
}
